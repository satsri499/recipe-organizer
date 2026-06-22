package com.example.demo.service;

import com.example.demo.dto.ExtractedRecipeData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.*;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.TextBlockParam;

@Service
public class RecipeExtractionService {

    private final AnthropicClient anthropicClient;
    private final ObjectMapper objectMapper;
    private final YouTubeService youTubeService;


    public RecipeExtractionService(AnthropicClient anthropicClient,
                                   ObjectMapper objectMapper,
                                   YouTubeService youTubeService) {
        this.anthropicClient = anthropicClient;
        this.objectMapper = objectMapper;
        this.youTubeService = youTubeService;
    }

    // -------------------------------------------------------
    // Website URL
    // Fetch page content with Jsoup, send text to Claude
    // -------------------------------------------------------
    public ExtractedRecipeData extractFromWebsite(String url) throws Exception {
        System.out.println("Fetching webpage: " + url);

        org.jsoup.nodes.Document doc;

        try {
            doc = org.jsoup.Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Connection", "keep-alive")
                    .referrer("https://www.google.com")
                    .timeout(20000)
                    .followRedirects(true)
                    .ignoreHttpErrors(false)
                    .get();

            System.out.println("Page fetched successfully!");

        }  catch (java.net.SocketTimeoutException e) {
        throw new RuntimeException("Website took too long to respond. Please try a different URL.");

    } catch (java.io.IOException e) {
        throw new RuntimeException("Could not connect to website: " + e.getMessage());
    }
        /*catch (org.jsoup.HttpStatusException e) {
            if (e.getStatusCode() == 403) {
                throw new RuntimeException(
                        "This website doesn't allow recipe importing. " +
                                "Please try a different URL.");
            }
            throw new RuntimeException("Failed to fetch URL: " + e.getStatusCode());

        } catch (java.net.SocketTimeoutException e) {
            throw new RuntimeException(
                    "Website took too long to respond. Please try again.");
        }*/

        // Step 2 — Look for Recipe Schema
        String recipeContent = null;
        for (org.jsoup.nodes.Element script :
                doc.select("script[type=application/ld+json]")) {
            String json = script.html();
            if (json.contains("Recipe")) {
                recipeContent = json;
                System.out.println("Found Recipe Schema JSON!");
                break;
            }
        }

        // Step 3 — Fallback to page text
        if (recipeContent == null) {
            System.out.println("No Recipe Schema — using page text");
            recipeContent = doc.body().text();
            if (recipeContent.length() > 3000) {
                recipeContent = recipeContent.substring(0, 3000);
            }
        }

        System.out.println("Sending to Claude...");

        // Step 4 — Send to Claude
        String prompt = buildPrompt(recipeContent);
        Message response = anthropicClient.messages().create(
                MessageCreateParams.builder()
                        .model("claude-haiku-4-5")
                        .maxTokens(2000L)
                        .addUserMessage(prompt)
                        .build()
        );

        String json = extractText(response);
        return parseJson(json);
    }
    // -------------------------------------------------------
    // Phase 3 — Image upload
    // Save image, send to Claude vision
    // -------------------------------------------------------
    public List<ExtractedRecipeData> extractFromImages(List<MultipartFile> imageFiles) throws Exception {
        System.out.println("Extracting recipe from " + imageFiles.size() + " image(s)");

        List<ContentBlockParam> contentBlocks = new ArrayList<>();

        // 1. Add each image as a proper image block
        for (MultipartFile file : imageFiles) {
            String mediaType = file.getContentType();
            if (mediaType == null || !mediaType.startsWith("image/")) {
                throw new RuntimeException(
                        "File '" + file.getOriginalFilename() + "' is not a valid image.");
            }

            // Compress image
            byte[] compressed = compressImage(file.getBytes());
            String base64 = java.util.Base64.getEncoder().encodeToString(compressed);
            System.out.println("Image ready: " + compressed.length + " bytes");

            // Add image block using correct SDK method
            contentBlocks.add(ContentBlockParam.ofImage(
                    ImageBlockParam.builder()
                            .source(Base64ImageSource.builder()
                                    .data(base64)
                                    .mediaType(Base64ImageSource.MediaType.IMAGE_JPEG)
                                    .build())
                            .build()
            ));
        }

        // 2. Add text prompt block
        String contextMsg = imageFiles.size() > 1
                ? "I am sending " + imageFiles.size() + " images that may show parts of the same recipe or different recipes."
                : "I am sending 1 food image.";

        contentBlocks.add(ContentBlockParam.ofText(
                TextBlockParam.builder()
                        .text(buildMultiImagePrompt(contextMsg))
                        .build()
        ));

        // 3. Call Claude with proper image blocks
        Message response = anthropicClient.messages().create(
                MessageCreateParams.builder()
                        .model("claude-haiku-4-5")
                        .maxTokens(4000L)
                        .addUserMessageOfBlockParams(contentBlocks)
                        .build()
        );

        String json = extractText(response);
        return parseMultipleRecipes(json);
    }
    // -------------------------------------------------------
    // Phase 4 — YouTube
    // Fetch video description, check if recipe exists,
    // if yes send to Claude, if no tell the user
    // -------------------------------------------------------
    public ExtractedRecipeData extractFromYoutube(String url) throws Exception {
        System.out.println("Processing YouTube URL: " + url);

        // 1. Fetch video details
        YouTubeService.VideoDetails video = youTubeService.fetchVideoDetails(url);

        // 2. Check if description has a recipe
        if (!youTubeService.containsRecipe(video.description)) {
            throw new RuntimeException(
                    "No recipe found in the description of '" + video.title + "'. " +
                            "The video creator hasn't included the recipe in the description. " +
                            "Try a different video or add the recipe manually.");
        }

        System.out.println("Recipe found! Sending to Claude...");

        // 3. Build content to send to Claude
        String content = """
        Video Title: %s
        Channel: %s
        
        Video Description:
        %s
        """.formatted(video.title, video.channelName,
                video.description != null ? video.description : "");

        // 4. Limit to 4000 chars to control API cost
        if (content.length() > 4000) {
            content = content.substring(0, 4000);
            System.out.println("Description truncated to 4000 chars");
        }

        // 5. Send to Claude
        String prompt = buildPrompt(content);
        Message response = anthropicClient.messages().create(
                MessageCreateParams.builder()
                        .model("claude-haiku-4-5")
                        .maxTokens(2000L)
                        .addUserMessage(prompt)
                        .build()
        );

        String json = extractText(response);
        return parseJson(json);
    }

    // -------------------------------------------------------
    // Shared — build prompt for Claude
    // -------------------------------------------------------
    protected String buildPrompt(String content) {
        return """
            Based on the following content, extract the recipe details.
            
            Content:
            %s
            
            You MUST respond with ONLY a valid JSON object.
            No explanation, no markdown, no code fences.
            
            If this content does not contain a recipe, respond with:
            { "error": "No recipe found in the provided content" }
            
            Otherwise respond with EXACTLY this structure:
            {
              "name": "Recipe Name",
              "description": "Brief one line description",
              "cuisine": "Italian",
              "category": "DINNER",
              "servings": 2,
              "calories": 450,
              "proteinG": 35,
              "carbsG": 30,
              "fatG": 15,
              "fiberG": 4,
              "sugarG": 3,
              "ingredients": [
                { "name": "chicken breast", "quantity": "200", "unit": "g", "orderNum": 1 }
              ],
              "steps": [
                { "stepNumber": 1, "instruction": "Heat oil in pan", "durationMins": 2 }
              ],
              "tags": ["high-protein", "gluten-free"]
            }
            
            Rules:
            - category must be one of: BREAKFAST, LUNCH, DINNER, SNACKS
            - All nutrition values must be integers
            - durationMins can be null if step has no specific time
            - Return ONLY the JSON, nothing else
            """.formatted(content);
    }

    // -------------------------------------------------------
    // Shared — extract text from Claude response
    // -------------------------------------------------------
    protected String extractText(Message message) {
        String result = message.content().get(0)
                .text()
                .map(t -> t.text())
                .orElse("");

        System.out.println("Claude raw response: " + result);

        return result
                .replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();
    }

    // -------------------------------------------------------
    // Shared — parse Claude JSON response
    // Throws exception if Claude says no recipe found
    // -------------------------------------------------------
    protected ExtractedRecipeData parseJson(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json);

        // Check if Claude couldn't find a recipe
        if (root.has("error")) {
            throw new RuntimeException(root.path("error").asText());
        }

        ExtractedRecipeData data = new ExtractedRecipeData();
        data.setName(root.path("name").asText());
        data.setDescription(root.path("description").asText());
        data.setCuisine(root.path("cuisine").asText());
        data.setCategory(root.path("category").asText("DINNER"));
        data.setServings(root.path("servings").asInt(2));
        data.setCalories(root.path("calories").asInt(0));
        data.setProteinG(root.path("proteinG").asInt(0));
        data.setCarbsG(root.path("carbsG").asInt(0));
        data.setFatG(root.path("fatG").asInt(0));
        data.setFiberG(root.path("fiberG").asInt(0));
        data.setSugarG(root.path("sugarG").asInt(0));

        List<ExtractedRecipeData.IngredientData> ingredients = new ArrayList<>();
        JsonNode ingredientsNode = root.path("ingredients");
        if (ingredientsNode.isArray()) {
            for (JsonNode ing : ingredientsNode) {
                ingredients.add(new ExtractedRecipeData.IngredientData(
                        ing.path("name").asText(),
                        ing.path("quantity").asText(),
                        ing.path("unit").asText(),
                        ing.path("orderNum").asInt()
                ));
            }
        }
        data.setIngredients(ingredients);

        List<ExtractedRecipeData.StepData> steps = new ArrayList<>();
        JsonNode stepsNode = root.path("steps");
        if (stepsNode.isArray()) {
            for (JsonNode step : stepsNode) {
                Integer duration = step.path("durationMins").isNull() ?
                        null : step.path("durationMins").asInt();
                steps.add(new ExtractedRecipeData.StepData(
                        step.path("stepNumber").asInt(),
                        step.path("instruction").asText(),
                        duration
                ));
            }
        }
        data.setSteps(steps);

        List<String> tags = new ArrayList<>();
        JsonNode tagsNode = root.path("tags");
        if (tagsNode.isArray()) {
            for (JsonNode tag : tagsNode) {
                tags.add(tag.asText());
            }
        }
        data.setTags(tags);

        System.out.println("Parsed recipe: " + data.getName());
        return data;
    }
    private String buildMultiImagePrompt(String context) {
        return """
        %s
        
        IMPORTANT: Read the image very carefully.
        - Read ALL ingredients exactly as written including quantities
        - Pay special attention to fractions (½, ¼, ⅓ etc.)
        - Do NOT skip any ingredients
        - Do NOT change quantities
        - If you cannot read something clearly, write exactly what you can see
        
        Extract recipe(s) from the image(s). Return ONLY a JSON array:
        
        [{"name":"","description":"","cuisine":"","category":"DINNER",
        "servings":2,"calories":0,"proteinG":0,"carbsG":0,"fatG":0,
        "fiberG":0,"sugarG":0,
        "ingredients":[{"name":"","quantity":"","unit":"","orderNum":1}],
        "steps":[{"stepNumber":1,"instruction":"","durationMins":null}],
        "tags":[]}]
        
        If no recipe found return: []
        category: BREAKFAST/LUNCH/DINNER/SNACKS only.
        Return ONLY the JSON array.
        """.formatted(context);
    }
    private List<ExtractedRecipeData> parseMultipleRecipes(String json) throws Exception {
        // Safety check — if JSON is truncated, try to fix it
        if (!json.trim().endsWith("]")) {
            System.out.println("Warning: JSON response appears truncated — increasing maxTokens may help");
            // Try to salvage what we have by closing the JSON
            int lastCompleteObject = json.lastIndexOf("},");
            if (lastCompleteObject > 0) {
                json = json.substring(0, lastCompleteObject + 1) + "]";
                System.out.println("Salvaged partial JSON response");
            } else {
                throw new RuntimeException(
                        "Could not extract recipes — response was too long. Please try fewer images.");
            }
        }
        JsonNode root = objectMapper.readTree(json);

        // Empty array — no recipes found
        if (!root.isArray() || root.size() == 0) {
            throw new RuntimeException(
                    "No recipe found in the provided image(s). " +
                            "Please try a clearer photo of a food dish or recipe.");
        }

        List<ExtractedRecipeData> recipes = new ArrayList<>();

        for (JsonNode recipeNode : root) {
            // Check if this node has an error
            if (recipeNode.has("error")) {
                System.out.println("Skipping recipe with error: " +
                        recipeNode.path("error").asText());
                continue;
            }

            ExtractedRecipeData data = new ExtractedRecipeData();
            data.setName(recipeNode.path("name").asText());
            data.setDescription(recipeNode.path("description").asText());
            data.setCuisine(recipeNode.path("cuisine").asText());
            data.setCategory(recipeNode.path("category").asText("DINNER"));
            data.setServings(recipeNode.path("servings").asInt(2));
            data.setCalories(recipeNode.path("calories").asInt(0));
            data.setProteinG(recipeNode.path("proteinG").asInt(0));
            data.setCarbsG(recipeNode.path("carbsG").asInt(0));
            data.setFatG(recipeNode.path("fatG").asInt(0));
            data.setFiberG(recipeNode.path("fiberG").asInt(0));
            data.setSugarG(recipeNode.path("sugarG").asInt(0));

            // Ingredients
            List<ExtractedRecipeData.IngredientData> ingredients = new ArrayList<>();
            JsonNode ingredientsNode = recipeNode.path("ingredients");
            if (ingredientsNode.isArray()) {
                for (JsonNode ing : ingredientsNode) {
                    ingredients.add(new ExtractedRecipeData.IngredientData(
                            ing.path("name").asText(),
                            ing.path("quantity").asText(),
                            ing.path("unit").asText(),
                            ing.path("orderNum").asInt()
                    ));
                }
            }
            data.setIngredients(ingredients);

            // Steps
            List<ExtractedRecipeData.StepData> steps = new ArrayList<>();
            JsonNode stepsNode = recipeNode.path("steps");
            if (stepsNode.isArray()) {
                for (JsonNode step : stepsNode) {
                    Integer duration = step.path("durationMins").isNull() ?
                            null : step.path("durationMins").asInt();
                    steps.add(new ExtractedRecipeData.StepData(
                            step.path("stepNumber").asInt(),
                            step.path("instruction").asText(),
                            duration
                    ));
                }
            }
            data.setSteps(steps);

            // Tags
            List<String> tags = new ArrayList<>();
            JsonNode tagsNode = recipeNode.path("tags");
            if (tagsNode.isArray()) {
                for (JsonNode tag : tagsNode) {
                    tags.add(tag.asText());
                }
            }
            data.setTags(tags);

            System.out.println("Parsed recipe: " + data.getName());
            recipes.add(data);
        }

        System.out.println("Total recipes found: " + recipes.size());
        return recipes;
    }
    private byte[] compressImage(byte[] imageBytes) throws Exception {
        java.awt.image.BufferedImage original = javax.imageio.ImageIO.read(
                new java.io.ByteArrayInputStream(imageBytes));

        if (original == null) {
            throw new RuntimeException("Cannot read image file. Please try a different image.");
        }

        // Target max 400px width
        int maxWidth = 600;//400
        int w = original.getWidth();
        int h = original.getHeight();
        int newW = Math.min(w, maxWidth);
        int newH = (int) ((double) h / w * newW);

        // Resize
        java.awt.Image scaled = original.getScaledInstance(
                newW, newH, java.awt.Image.SCALE_SMOOTH);
        java.awt.image.BufferedImage result = new java.awt.image.BufferedImage(
                newW, newH, java.awt.image.BufferedImage.TYPE_INT_RGB);
        result.getGraphics().drawImage(scaled, 0, 0, java.awt.Color.WHITE, null);

        // Write as JPEG with 40% quality
        javax.imageio.ImageWriter writer =
                javax.imageio.ImageIO.getImageWritersByFormatName("jpeg").next();
        javax.imageio.ImageWriteParam params = writer.getDefaultWriteParam();
        params.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
        params.setCompressionQuality(0.65f);  // was 0.4f


        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        writer.setOutput(javax.imageio.ImageIO.createImageOutputStream(outputStream));
        writer.write(null,
                new javax.imageio.IIOImage(result, null, null), params);

        byte[] compressed = outputStream.toByteArray();
        System.out.println("Compressed: " + imageBytes.length +
                " → " + compressed.length + " bytes");
        return compressed;
    }
}
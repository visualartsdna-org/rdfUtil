package rdf.util

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

/**
 * code by claude.ai
 */
class JsonLd11To10Converter {
    
    /**
     * Converts JSON-LD 1.1 format to JSON-LD 1.0 format
     * @param jsonLd11String JSON-LD 1.1 as String
     * @return JSON-LD 1.0 as String
     */
    static String convert(String jsonLd11String) {
        def slurper = new JsonSlurper()
        def jsonLd = slurper.parseText(jsonLd11String)
        
        def converted = convertObject(jsonLd)
        
        return JsonOutput.prettyPrint(JsonOutput.toJson(converted))
    }
    
    /**
     * Converts JSON-LD 1.1 object to JSON-LD 1.0 object
     * @param obj The JSON object to convert
     * @return Converted object
     */
    private static Object convertObject(Object obj) {
        if (obj instanceof Map) {
            return convertMap(obj)
        } else if (obj instanceof List) {
            return obj.collect { convertObject(it) }
        }
        return obj
    }
    
    /**
     * Converts a map representing JSON-LD 1.1 to 1.0
     */
    private static Map convertMap(Map jsonLd) {
        Map result = [:]
        
        // Handle @context
        if (jsonLd.containsKey('@context')) {
            result['@context'] = convertContext(jsonLd['@context'])
        }
        
        // Check if this is a root object without @graph
        // In 1.0, we need to wrap non-graph objects in @graph array
        boolean hasGraph = jsonLd.containsKey('@graph')
        boolean hasId = jsonLd.containsKey('@id')
        boolean hasType = jsonLd.containsKey('@type')
        boolean hasOtherProperties = jsonLd.keySet().any { 
            it != '@context' && it != '@graph' && it != '@id' && it != '@type' 
        }
        
        // If it's a single resource at root level (1.1 style), wrap it in @graph (1.0 style)
        if (!hasGraph && (hasId || hasType || hasOtherProperties)) {
            def graphContent = [:]
            jsonLd.each { key, value ->
                if (key != '@context') {
                    graphContent[key] = convertValue(value)
                }
            }
            result['@graph'] = [graphContent]
        } else if (hasGraph) {
            // Process @graph array
            result['@graph'] = convertValue(jsonLd['@graph'])
        } else {
            // Process other properties
            jsonLd.each { key, value ->
                if (key != '@context') {
                    result[key] = convertValue(value)
                }
            }
        }
        
        return result
    }
    
    /**
     * Converts context, handling JSON-LD 1.1 features
     */
    private static Object convertContext(Object context) {
        if (context instanceof String) {
            return context
        } else if (context instanceof List) {
            // Handle array of contexts (1.1 feature)
            // Merge them into a single context for 1.0
            return mergeContexts(context)
        } else if (context instanceof Map) {
            Map result = [:]
            context.each { key, value ->
                // Remove @version if present (1.1 feature)
                if (key == '@version') {
                    return
                }
                
                // Handle nested context definitions
                if (value instanceof Map) {
                    // Remove 1.1-specific properties like @protected, @container: @graph, etc.
                    Map cleanValue = [:]
                    value.each { k, v ->
                        if (k != '@protected' && k != '@propagate') {
                            // Handle @container changes
                            if (k == '@container') {
                                cleanValue[k] = convertContainerValue(v)
                            } else {
                                cleanValue[k] = v
                            }
                        }
                    }
                    result[key] = cleanValue
                } else {
                    result[key] = value
                }
            }
            return result
        }
        return context
    }
    
    /**
     * Converts @container values from 1.1 to 1.0 compatible format
     */
    private static Object convertContainerValue(Object container) {
        if (container instanceof String) {
            // Remove unsupported 1.0 containers like @graph, @id, @type when used alone
            if (container in ['@graph', '@id', '@type']) {
                return '@set' // Fallback to @set
            }
            return container
        } else if (container instanceof List) {
            // 1.1 allows arrays for @container, 1.0 doesn't
            // Take the first compatible value or default to @set
            def compatible = container.find { it in ['@set', '@list', '@language', '@index'] }
            return compatible ?: '@set'
        }
        return container
    }
    
    /**
     * Merges multiple contexts into one for 1.0 compatibility
     */
    private static Map mergeContexts(List contexts) {
        Map merged = [:]
        contexts.each { ctx ->
            if (ctx instanceof Map) {
                ctx.each { key, value ->
                    if (key != '@version') {
                        merged[key] = value
                    }
                }
            } else if (ctx instanceof String) {
                // For string contexts (URLs), we'd need to fetch and merge
                // For simplicity, just note it in a comment or handle as needed
                // In production, you might want to fetch these
            }
        }
        return merged
    }
    
    /**
     * Recursively converts values
     */
    private static Object convertValue(Object value) {
        if (value instanceof Map) {
            Map result = [:]
            value.each { k, v ->
                // Handle @json (1.1 feature) - convert to string or remove
                if (k == '@json') {
                    return // Skip @json containers
                }
                result[k] = convertValue(v)
            }
            return result
        } else if (value instanceof List) {
            return value.collect { convertValue(it) }
        }
        return value
    }
    
    /**
     * Convenience method to convert from File
     */
    static String convertFile(File inputFile) {
        return convert(inputFile.text)
    }
    
    /**
     * Convenience method to convert and save to file
     */
    static void convertAndSave(String inputPath, String outputPath) {
        def input = new File(inputPath).text
        def output = convert(input)
        new File(outputPath).text = output
    }
}


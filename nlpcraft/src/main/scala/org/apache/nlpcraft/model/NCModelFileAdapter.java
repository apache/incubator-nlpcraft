/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.model;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.*;
import com.google.gson.*;
import org.apache.nlpcraft.common.*;
import org.apache.nlpcraft.common.util.*;
import org.apache.nlpcraft.model.impl.json.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.*;
/**
 * Adapter for data models that can load model configuration from external JSON/YAML file.
 * <p>
 * One of the use cases this adapter supports is ability to load model configuration from the external
 * JSON/YAML file and then update it in the code. For example, a model can load its configuration
 * from JSON file and then add intents or synonyms loaded from a database to a certain model element.
 * To support this usage all getters return internal mutable sets or maps, i.e. you can modify them in your sub-class
 * constructors and those modifications will alter the model's configuration.
 * <p>
 * Read full documentation in <a target=_ href="https://nlpcraft.apache.org/data-model.html">Data Model</a> section and review
 * <a target=_ href="https://github.com/apache/incubator-nlpcraft/tree/master/nlpcraft/src/main/scala/org/apache/nlpcraft/examples/">examples</a>.
 *
 * @see NCModelAdapter
 */
abstract public class NCModelFileAdapter extends NCModelAdapter {
    private final NCModelJson proxy;
    private final Set<String> suspWords;
    private final Set<String> enabledToks;
    private final Set<String> addStopwords;
    private final Set<String> exclStopwords;
    private final Set<String> intents;
    private final Map<String, String> macros;
    private final Map<String, Object> metadata;
    private final Set<NCElement> elems;
    private final List<NCCustomParser> parsers;

    private final String origin;

    /** */
    private static final Gson GSON = new Gson();
    
    /**
     * Creates new model loading its configuration from given file path. Only <code>.js</code>,
     * <code>.json</code>, <code>.yml</code> and <code>.yaml</code> files are supported. File path can be
     * classpath relative or absolute.
     *
     * @param filePath Classpath relative or absolute file path to load model configuration from.
     * @throws NCException Thrown in case of any errors loading model configuration.
     */
    public NCModelFileAdapter(String filePath) {
        this(mkProxy(filePath), filePath);
    }
    
    /**
     * Creates new model loading its configuration from given URI. Only <code>.js</code>,
     * <code>.json</code>, <code>.yml</code> and <code>.yaml</code> resources are supported.
     *
     * @param uri URI to load model configuration from.
     * @throws NCException Thrown in case of any errors loading model configuration.
     */
    public NCModelFileAdapter(URI uri) {
        this(mkProxy(uri), uri.toString());
    }
    
    /**
     *
     * @param proxy
     * @param origin
     * @throws NCException Thrown in case of any errors intents definition.
     */
    private NCModelFileAdapter(NCModelJson proxy, String origin) throws NCException {
        super(proxy.getId(), proxy.getName(), proxy.getDescription());

        this.proxy = proxy;
        this.suspWords = convert(proxy.getSuspiciousWords());
        this.enabledToks = convert(proxy.getEnabledBuiltInTokens(), DFLT_ENABLED_BUILTIN_TOKENS);
        this.addStopwords = convert(proxy.getAdditionalStopwords());
        this.exclStopwords = convert(proxy.getExcludedStopwords());
        this.elems = convertElements(proxy.getElements());
        this.macros = convertMacros(proxy.getMacros());
        this.metadata = convertMeta(proxy.getMetadata());
        this.intents = convert(proxy.getIntents());
        this.parsers = convertParsers(proxy.getParsers());

        // NOTE: we can only test/check this at this point. Downstream - this information is lost.
        if (proxy.getIntents() != null && intents.size() != proxy.getIntents().length)
            throw new NCException("Model contains duplicate intents: " + origin);

        this.origin = origin;
    }

    /**
     *
     * @param filePath
     * @return
     */
    private static NCModelJson mkProxy(String filePath) {
        // Try on-classpath first.
        InputStream in = NCModelFileAdapter.class.getClassLoader().getResourceAsStream(filePath);
        
        if (in == null)
            try {
                in = new FileInputStream(new File(filePath));
            }
            catch (FileNotFoundException e) {
                // Ignore.
            }
        
        if (in == null)
            throw new NCException("Model configuration file path not found: " + filePath);
        
        return readModel(filePath, in, filePath.toLowerCase());
    }
    
    /**
     *
     * @param uri
     * @return
     * @throws NCException
     */
    private static NCModelJson mkProxy(URI uri) {
        try {
            return readModel(uri.toString(), uri.toURL().openStream(), uri.getPath().toLowerCase());
        }
        catch (MalformedURLException e) {
            throw new NCException("Malformed model configuration URI: " + uri.toString(), e);
        }
        catch (IOException e) {
            throw new NCException("Failed to read model configuration from: " + uri.toString(), e);
        }
    }
    
    /**
     *
     * @param path
     * @param in
     * @param pathLow
     * @return
     * @throws NCException
     */
    private static NCModelJson readModel(String path, InputStream in, String pathLow) {
        if (pathLow.endsWith("yaml") || pathLow.endsWith("yml")) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    
            try {
                return mapper.readValue(in, NCModelJson.class);
            }
            catch (Exception e) {
                throw new NCException("Failed to load YAML from: " + path, e);
            }
        }
        else if (pathLow.endsWith("js") || pathLow.endsWith("json")) {
            try (Reader reader = new BufferedReader(new InputStreamReader(in))) {
                return GSON.fromJson(reader, NCModelJson.class);
            }
            catch (Exception e) {
                throw new NCException("Failed to load JSON from: " + path, e);
            }
        }
        
        throw new NCException("Unsupported model configuration file type (.yaml, .yml, .js or .json only): " + path);
    }
    
    /**
     *
     * @param arr
     * @return
     */
    private static Set<String> convert(String[] arr) {
        return arr == null ? new HashSet<>() : new HashSet<>(Arrays.asList(arr));
    }

    /**
     *
     * @param arr
     * @return
     */
    private static List<NCCustomParser> convertParsers(String[] arr) {
        return arr == null ?
            new ArrayList<>() :
            Arrays.stream(arr).map(p -> (NCCustomParser)NCUtils.mkObject(p)).collect(Collectors.toList());
    }

    /**
     *
     * @param arr
     * @param dflt
     * @return
     */
    private static Set<String> convert(String[] arr, Set<String> dflt) {
        return arr == null ? new HashSet<>(dflt) : new HashSet<>(Arrays.asList(arr));
    }

    /**
     *
     * @param arr
     * @return
     */
    private static Map<String, String> convertMacros(NCMacroJson[] arr) {
        return
            arr == null ?
                new HashMap<>() :
                Arrays.stream(arr).collect(Collectors.toMap(NCMacroJson::getName, NCMacroJson::getMacro));
    }

    /**
     *
     * @param m
     * @return
     */
    private static Map<String, Object> convertMeta(Map<String, Object> m) {
        return m != null ? m : new HashMap<>();
    }

    /**
     *
     * @param arr
     * @return
     */
    private static Set<NCElement> convertElements(NCElementJson[] arr) {
        if (arr == null)
            return Collections.emptySet();

        Map<String, NCValueLoader> loaders = new HashMap<>();

        return
            Arrays.stream(arr).map(js -> {
                List<String> syns = Arrays.asList(js.getSynonyms());
                List<NCValue> vals = Arrays.stream(js.getValues()).map(e ->
                    new NCValue() {
                        @Override
                        public String getName() {
                            return e.getName();
                        }

                        @Override
                        public List<String> getSynonyms() {
                            return Arrays.asList(e.getSynonyms());
                        }
                    })
                    .collect(Collectors.toList());

                List<String> groups = js.getGroups() == null ? Collections.singletonList(js.getId()) : Arrays.asList(js.getGroups());

                Map<String, Object> md = js.getMetadata();

                return
                    new NCElement() {
                        @Override
                        public String getId() {
                            return js.getId();
                        }

                        @Override
                        public List<String> getGroups() {
                            return groups;
                        }

                        @Override
                        public Map<String, Object> getMetadata() {
                            return md;
                        }

                        @Override
                        public String getDescription() {
                            return js.getDescription();
                        }

                        @Override
                        public List<NCValue> getValues() {
                            return vals;
                        }

                        @Override
                        public String getParentId() {
                            return js.getParentId();
                        }

                        @Override
                        public List<String> getSynonyms() {
                            return syns;
                        }

                        private NCValueLoader mkLoader(String clsName) {
                            NCValueLoader ldr = NCUtils.mkObject(clsName);

                            ldr.onInit();

                            return ldr;
                        }

                        @Override
                        public NCValueLoader getValueLoader() {
                            return js.getValueLoader() != null ?
                                loaders.computeIfAbsent(js.getValueLoader(), this::mkLoader) :
                                null;
                        }
                    };
            }).collect(Collectors.toSet());
    }

    /**
     * Gets this file model adapter origin (file path or URI).
     *
     * @return This file model adapter origin (file path or URI).
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Gets list of intents declared in JSON/YML model definition, if any.
     *
     * @return List of intents declared in JSON/YML model definition, potentially empty.
     */
    public Set<String> getIntents() {
        return intents;
    }

    @Override
    public String getId() {
        return proxy.getId();
    }

    @Override
    public String getName() {
        return proxy.getName();
    }

    @Override
    public String getVersion() {
        return proxy.getVersion();
    }

    @Override
    public String getDescription() {
        return proxy.getDescription();
    }

    @Override
    public int getMaxUnknownWords() {
        return proxy.getMaxUnknownWords();
    }

    @Override
    public int getMaxFreeWords() {
        return proxy.getMaxFreeWords();
    }

    @Override
    public int getMaxSuspiciousWords() {
        return proxy.getMaxSuspiciousWords();
    }

    @Override
    public int getMinWords() {
        return proxy.getMinWords();
    }

    @Override
    public int getMaxWords() {
        return proxy.getMaxWords();
    }

    @Override
    public int getMinTokens() {
        return proxy.getMinTokens();
    }

    @Override
    public int getMaxTokens() {
        return proxy.getMaxTokens();
    }

    @Override
    public int getMinNonStopwords() {
        return proxy.getMinNonStopwords();
    }

    @Override
    public boolean isNonEnglishAllowed() {
        return proxy.isNonEnglishAllowed();
    }

    @Override
    public boolean isNotLatinCharsetAllowed() {
        return proxy.isNotLatinCharsetAllowed();
    }

    @Override
    public boolean isSwearWordsAllowed() {
        return proxy.isSwearWordsAllowed();
    }

    @Override
    public boolean isNoNounsAllowed() {
        return proxy.isNoNounsAllowed();
    }

    @Override
    public boolean isPermutateSynonyms() {
        return proxy.isPermutateSynonyms();
    }

    @Override
    public boolean isDupSynonymsAllowed() {
        return proxy.isDupSynonymsAllowed();
    }

    @Override
    public int getMaxTotalSynonyms() {
        return proxy.getMaxTotalSynonyms();
    }

    @Override
    public boolean isNoUserTokensAllowed() {
        return proxy.isNoUserTokensAllowed();
    }

    @Override
    public int getJiggleFactor() {
        return proxy.getJiggleFactor();
    }

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public Set<String> getAdditionalStopWords() {
        return addStopwords;
    }

    @Override
    public Set<String> getExcludedStopWords() {
        return exclStopwords;
    }

    @Override
    public Set<String> getSuspiciousWords() {
        return suspWords;
    }

    @Override
    public Map<String, String> getMacros() {
        return macros;
    }

    @Override
    public Set<NCElement> getElements() {
        return elems;
    }

    @Override
    public Set<String> getEnabledBuiltInTokens() {
       return enabledToks;
    }

    @Override
    public List<NCCustomParser> getParsers() {
        return parsers;
    }
}
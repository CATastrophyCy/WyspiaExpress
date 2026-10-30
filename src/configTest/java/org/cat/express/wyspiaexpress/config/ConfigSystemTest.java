package org.cat.express.wyspiaexpress.config;

import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.util.zip.*;

/** Dependency-free regression entry point, run by Gradle verifyConfig and check. */
public final class ConfigSystemTest {
    private static int checks;
    public static class ExampleModeSettings {
        @ConfigRange(min = 1, max = 60)
        public int spawnDelaySeconds = 12;
    }
    private static void check(boolean condition, String reason) {
        if (!condition) throw new AssertionError(reason);
        checks++;
    }
    private static void rejects(Runnable action, String reason) {
        try { action.run(); }
        catch (IllegalArgumentException expected) { checks++; return; }
        throw new AssertionError("Accepted invalid configuration: " + reason);
    }
    private static void write(Path directory, String file, String text) throws Exception {
        Path path = directory.resolve(file);
        Files.createDirectories(path.getParent());
        Files.writeString(path, text);
    }
    public static void main(String[] args) throws Exception {
        compilerRejects("@ConfigModel(\"Duplicate\") public static class A { public int x=1; } @ConfigModel(\"Duplicate\") public static class B { public int y=2; }", "already generated");
        compilerRejects("@ConfigModel(\"Recursive\") public static class A { public A self=this; }", "Recursive model");
        compilerRejects("@ConfigModel(\"Unsupported\") public static class A { public byte x=1; }", "Unsupported config type");
        compilerRejects("@ConfigModel(\"switch\") public static class A { public int x=1; }", "Invalid view name");
        Object sharedModel = new GeneralConfigModel();
        rejects(() -> new ConfigSchema(Map.of("first", sharedModel, "second", sharedModel)), "shared model instances rejected");
        var generated = new ConfigAnnotationTest();
        check(generated.count() == 3, "generated primitive getter");
        check(generated.items().getFirst() == org.cat.express.wyspiaexpress.shop.EnumShopEntry.KNIFE, "generated generic enum-list getter");
        check(generated.nested.enabled(), "generated nested model getter");
        check(generated.model().count == 3, "generated view exposes its declaration model");
        var roots = new LinkedHashMap<String, Object>();
        roots.put("general", new GeneralConfigModel()); roots.put("server", new ServerConfigModel());
        roots.put("rules", new RulesConfigModel()); roots.put("tasks", new TasksConfigModel());
        roots.put("gamemodes", new GamemodesConfigModel()); roots.put("roles", new RolesConfigModel());
        roots.put("items", new ItemsConfigModel()); roots.put("modifiers", new ModifiersConfigModel());
        roots.put("mode", new ModeConfigModel());
        var schema = new ConfigSchema(roots);
        schema.registerModeSettings("example:nested/mode", new ExampleModeSettings());
        var defaults=schema.defaults();
        var decoded=schema.decodeComplete(defaults,"defaults");
        check(decoded.size()>500,"complete schema collected");
        check(decoded.get("tasks.minTaskDelaySeconds").equals(30),"typed int");
        check(decoded.get("mode.hideGamemode").equals(false),"gamemode role restrictions visible by default");
        check(decoded.get("rules.maxTimeForDeathBonus").equals(-1),"civilian death time limit disabled by default");
        check(decoded.get("items.itemConfig.grenadeConfig.showTrajectory").equals(true),"grenade preview enabled by default");
        check(decoded.get("items.itemConfig.smokeBombConfig.showTrajectory").equals(true),"smoke bomb preview enabled by default");
        check(decoded.get("roles.roleConfig.gamblerConfig.goodPool") instanceof List<?>,"typed enum list");
        for(var entry:defaults.entrySet()){
            var changed=new TreeMap<>(defaults);changed.put(entry.getKey(),JsonNull.INSTANCE);
            rejects(()->schema.decodeComplete(changed,"null test"),entry.getKey());
        }
        var values=new TreeMap<>(defaults);
        schema.overlay("items",JsonParser.parseString("{\"itemConfig\":{\"grenadeConfig\":{\"showTrajectory\":false}}}").getAsJsonObject(),values,"grenade preview override");
        check(schema.decodeComplete(values,"preview disabled").get("items.itemConfig.grenadeConfig.showTrajectory").equals(false),"grenade preview can be disabled independently");
        check(schema.decodeComplete(values,"smoke preview unchanged").get("items.itemConfig.smokeBombConfig.showTrajectory").equals(true),"grenade toggle leaves smoke bomb preview enabled");
        schema.overlay("items",JsonParser.parseString("{\"itemConfig\":{\"smokeBombConfig\":{\"showTrajectory\":false}}}").getAsJsonObject(),values,"smoke preview override");
        check(schema.decodeComplete(values,"smoke preview disabled").get("items.itemConfig.smokeBombConfig.showTrajectory").equals(false),"smoke bomb preview can be disabled independently");
        check(schema.decodeComplete(values,"preview disabled").get("items.itemConfig.grenadeConfig.radius").equals(decoded.get("items.itemConfig.grenadeConfig.radius")),"preview override retains blast radius");
        schema.overlay("rules",JsonParser.parseString("{\"freeze\":false,\"blockStunTicks\":0}").getAsJsonObject(),values,"explicit false/zero");
        check(schema.decodeComplete(values,"explicit").get("rules.blockStunTicks").equals(0),"zero retained");
        rejects(()->schema.overlay("rules",JsonParser.parseString("{\"freeez\":true}").getAsJsonObject(),values,"typo"),"unknown field");
        rejects(()->schema.overlay("rules",JsonParser.parseString("{\"freeze\":\"false\"}").getAsJsonObject(),values,"type"),"string boolean");
        rejects(()->schema.overlay("rules",JsonParser.parseString("{\"freezeTimer\":2.5}").getAsJsonObject(),values,"fraction"),"fractional integer");
        rejects(()->schema.overlay("tasks",JsonParser.parseString("{\"taskMoodReward\":2}").getAsJsonObject(),values,"range"),"range constraint");
        rejects(()->schema.overlay("rules",JsonParser.parseString("{\"maxTimeForDeathBonus\":-2}").getAsJsonObject(),values,"death bonus limit"),"death bonus limit below disabled sentinel");
        rejects(()->schema.overlay("rules",JsonParser.parseString("{\"maxTimeForDeathBonus\":1789570}").getAsJsonObject(),values,"death bonus limit overflow"),"death bonus limit must fit timer ticks");
        var crossed=new TreeMap<>(defaults);crossed.put("tasks.minTaskDelaySeconds",new JsonPrimitive(100));
        rejects(()->schema.decodeComplete(crossed,"cross-field"),"min above max");
        var missing=new TreeMap<>(defaults);missing.remove("rules.freeze");rejects(()->schema.decodeComplete(missing,"missing"),"incomplete snapshot");
        var extra=new TreeMap<>(defaults);extra.put("rules.typo",new JsonPrimitive(1));rejects(()->schema.decodeComplete(extra,"extra"),"extra snapshot setting");
        Path dir=Files.createTempDirectory("wyspia-config-checks");
        write(dir,"rules.json5","// shared\n{freeze:true,freezeTimer:73}");
        write(dir,"roles.json5","{roleConfig:{outlawConfig:{cooldown:81,basic:{items:[\"KNIFE\"],itemAmount:[2]}}}}");
        write(dir,"gamemodes/wyspiaexpress/killer_modded/rules.json5","{freeze:false,maxTimeForDeathBonus:6}");
        write(dir,"gamemodes/wyspiaexpress/killer_modded/mode.json5","{hideGamemode:true}");
        write(dir,"gamemodes/wyspiaexpress/killer_modded/roles.json5","{roleConfig:{outlawConfig:{cooldown:0,basic:{items:[]}}}}");
        String authored=Files.readString(dir.resolve("gamemodes/wyspiaexpress/killer_modded/roles.json5"));
        var profiles=ConfigFiles.load(dir,schema);
        check(profiles.get("shared").get("rules.freeze").equals(true),"shared value");
        var mode=profiles.get("wyspiaexpress:killer_modded");
        check(mode.get("mode.hideGamemode").equals(true),"gamemode visibility overridden per mode");
        check(profiles.get("shared").get("mode.hideGamemode").equals(false),"mode visibility override leaves shared default intact");
        check(mode.get("mode.killerDividend").equals(profiles.get("shared").get("mode.killerDividend")),"visibility override inherits other mode settings");
        check(mode.get("rules.freeze").equals(false),"false override");
        check(mode.get("rules.maxTimeForDeathBonus").equals(6),"mode overrides civilian death time limit");
        check(profiles.get("shared").get("rules.maxTimeForDeathBonus").equals(-1),"mode death bonus limit does not change shared default");
        check(mode.get("rules.freezeTimer").equals(73),"nested inheritance");
        check(mode.get("roles.roleConfig.outlawConfig.cooldown").equals(0),"zero cooldown");
        check(((List<?>)mode.get("roles.roleConfig.outlawConfig.basic.items")).isEmpty(),"empty replaces list");
        var sharedRoleValues = new TreeMap<>(defaults);
        schema.overlay("roles",JsonParser.parseString("""
                {"roleConfig":{"kinsWatheRoles":{"licensedVillainConfig":{"basic":{
                  "minimumPlayerSpawn":12,"maximumSpawn":2,"enableShop":true,"items":["KNIFE"],"itemAmount":[2]
                }}}}}
                """).getAsJsonObject(),sharedRoleValues,"nested shared role");
        var nestedOverride = new TreeMap<>(sharedRoleValues);
        schema.overlay("roles",JsonParser.parseString("""
                {"roleConfig":{"kinsWatheRoles":{
                  "licensedVillainConfig":{"basic":{"playersPerAdditionalSpawn":6,"maximumSpawn":3}},
                  "hackerConfig":{"basic":{"minimumPlayerSpawn":99,"maximumSpawn":0}}
                }}}
                """).getAsJsonObject(),nestedOverride,"nested role override");
        var resolvedNested = schema.decodeComplete(nestedOverride,"nested role override");
        check(resolvedNested.get("roles.roleConfig.kinsWatheRoles.licensedVillainConfig.basic.playersPerAdditionalSpawn").equals(6),"one nested role field overridden");
        Set<String> changedRoleFields = Set.of(
                "roles.roleConfig.kinsWatheRoles.licensedVillainConfig.basic.playersPerAdditionalSpawn",
                "roles.roleConfig.kinsWatheRoles.licensedVillainConfig.basic.maximumSpawn",
                "roles.roleConfig.kinsWatheRoles.hackerConfig.basic.minimumPlayerSpawn",
                "roles.roleConfig.kinsWatheRoles.hackerConfig.basic.maximumSpawn");
        check(sharedRoleValues.entrySet().stream().filter(entry -> !changedRoleFields.contains(entry.getKey()))
                .allMatch(entry -> entry.getValue().equals(nestedOverride.get(entry.getKey()))),"all omitted nested fields and other roles inherited unchanged");
        var reorderedOverride = new TreeMap<>(sharedRoleValues);
        schema.overlay("roles",JsonParser.parseString("""
                {"roleConfig":{"kinsWatheRoles":{
                  "hackerConfig":{"basic":{"maximumSpawn":0,"minimumPlayerSpawn":99}},
                  "licensedVillainConfig":{"basic":{"maximumSpawn":3,"playersPerAdditionalSpawn":6}}
                }}}
                """).getAsJsonObject(),reorderedOverride,"reordered role override");
        check(schema.decodeComplete(reorderedOverride,"reordered role override").equals(resolvedNested),"role order and nested field order do not affect overrides");
        var emptyOverride = new TreeMap<>(sharedRoleValues);
        schema.overlay("roles",JsonParser.parseString("{\"roleConfig\":{\"kinsWatheRoles\":{\"licensedVillainConfig\":{\"basic\":{}}}}}").getAsJsonObject(),emptyOverride,"empty role override");
        check(emptyOverride.equals(sharedRoleValues),"empty nested objects preserve every inherited field");
        check(Files.readString(dir.resolve("gamemodes/wyspiaexpress/killer_modded/roles.json5")).equals(authored),"sparse file never expanded");
        String normalizedRules = Files.readString(dir.resolve("rules.json5"));
        check(ConfigFiles.read(dir.resolve("rules.json5")).has("depressedTimer"),"shared files save missing defaults");
        check(normalizedRules.contains("shared"),"authored shared field comment retained");
        check(normalizedRules.contains("The time, in seconds"),"generated field documentation retained");
        check(ConfigFiles.read(dir.resolve("roles.json5")).getAsJsonObject("roleConfig")
                .getAsJsonObject("outlawConfig").has("basic"),"nested shared settings serialized");
        String nestedShared = Files.readString(dir.resolve("roles.json5"));
        check(ConfigFiles.load(dir,schema).equals(profiles),"restart stable");
        check(Files.readString(dir.resolve("rules.json5")).equals(normalizedRules),"normalized shared file stable across loads");
        check(Files.readString(dir.resolve("roles.json5")).equals(nestedShared),"nested defaults and comments stable across loads");
        write(dir,"gamemodes/wyspiaexpress/killer_modded/roles.json5","{}");
        check(ConfigFiles.load(dir,schema).get("wyspiaexpress:killer_modded").get("roles.roleConfig.outlawConfig.cooldown").equals(81),"removing override restores inheritance");
        write(dir,"gamemodes/wyspiaexpress/killer_modded/rules.json5","{typo:1}");
        rejects(()->ConfigFiles.load(dir,schema),"invalid mode startup");
        check(Files.readString(dir.resolve("rules.json5")).equals(normalizedRules),"invalid override does not rewrite shared files");
        var bundle=ConfigBundle.fromProfiles(profiles,schema);
        var roundtrip=ConfigBundle.decode(bundle.compressed(),bundle.digest(),schema);
        for(String name:profiles.keySet())check(roundtrip.profile(name).equals(profiles.get(name)),"network equality "+name);
        check(roundtrip.profile("other:mode").equals(profiles.get("shared")),"unconfigured mode shared fallback");
        rejects(()->ConfigBundle.decode(bundle.compressed(),"0".repeat(64),schema),"digest mismatch");
        byte[] corrupt=bundle.compressed();corrupt[0]=0;rejects(()->ConfigBundle.decode(corrupt,bundle.digest(),schema),"corrupt gzip");
        rejects(()->ConfigBundle.decode(Arrays.copyOf(bundle.compressed(),10),bundle.digest(),schema),"truncated gzip");
        rejects(()->ConfigBundle.decode(new byte[ConfigBundle.MAX_BYTES+1],bundle.digest(),schema),"oversized transfer");
        try{roundtrip.profile("shared").put("rules.freeze",false);throw new AssertionError("mutable snapshot");}catch(UnsupportedOperationException expected){checks++;}

        Path nested = Files.createTempDirectory("wyspia-nested-mode");
        write(nested,"gamemodes/example/nested/mode/rules.json5","{freezeTimer:49}");
        check(ConfigFiles.load(nested,schema).get("example:nested/mode").get("rules.freezeTimer").equals(49),"namespaced nested mode paths");
        write(nested,"gamemodes/example/nested/mode/mode.json5","{spawnDelaySeconds:6}");
        check(ConfigFiles.load(nested,schema).get("example:nested/mode").get("modeSettings.example:nested/mode.spawnDelaySeconds").equals(6),"mode-specific typed schema override");
        check(!ConfigBundle.validModeId("example:../escape"),"mode path traversal rejected");
        var unknownShop = new TreeMap<>(defaults);
        unknownShop.put("roles.roleConfig.outlawConfig.basic.shopEntries", JsonParser.parseString("[\"TYPO;50;-1;WEAPON\"]"));
        rejects(() -> schema.decodeComplete(unknownShop, "shop typo"), "invalid shop item");
        var zeroDivisor = new TreeMap<>(defaults);
        zeroDivisor.put("mode.killerDividend", new JsonPrimitive(0));
        rejects(() -> schema.decodeComplete(zeroDivisor, "zero dividend"), "zero dividend");
        for (double weight : new double[]{0, -0.001}) {
            var invalidWeight = new TreeMap<>(defaults);
            invalidWeight.put("gamemodes.baseWeight", new JsonPrimitive(weight));
            rejects(() -> schema.decodeComplete(invalidWeight, "invalid base weight"), "nonpositive base weight");
        }
        var zeroTimer = new TreeMap<>(defaults);
        zeroTimer.put("rules.freezeTimer", new JsonPrimitive(0));
        rejects(() -> schema.decodeComplete(zeroTimer, "zero timer"), "zero freeze timer");
        Path oldServer = Files.createTempDirectory("wyspia-old-server");
        String old = "{freezeTimer:91,moodTasks:{minTaskDelaySeconds:32}}";
        write(oldServer,"general.json5",old);
        rejects(() -> ConfigFiles.load(oldServer,schema), "old server config is rejected, not migrated");
        check(Files.readString(oldServer.resolve("general.json5")).equals(old), "server file untouched on rejection");
        check(!Files.exists(oldServer.resolve("rules.json5")), "rejected server config creates no split files");
        try(var files=Files.list(oldServer)){check(files.count()==1,"no migration backups generated");}

        Path client = Files.createTempDirectory("wyspia-client-reset");
        write(client,"general.json5",old);
        write(client,"rules.json5","{freezeTimer:999}");
        write(client,"gamemodes/example/nested/mode/rules.json5","{freezeTimer:77}");
        write(client,"wyspiaexpress-guidebook.json","{\"background\":true}");
        ConfigFiles.prepareClient(client,schema,"test-1");
        check(ConfigFiles.read(client.resolve("rules.json5")).get("freezeTimer").getAsInt()==60,"first client run replaces old values with defaults");
        check(ConfigFiles.read(client.resolve("gamemodes/example/nested/mode/rules.json5")).isEmpty(),"client reset clears stale mode overrides");
        check(Files.readString(client.resolve("wyspiaexpress-guidebook.json")).equals("{\"background\":true}"),"balance reset preserves guidebook preferences");
        write(client,"rules.json5","{freezeTimer:87}");
        write(client,"gamemodes/example/nested/mode/rules.json5","{freezeTimer:81}");
        ConfigFiles.prepareClient(client,schema,"test-1");
        check(ConfigFiles.read(client.resolve("rules.json5")).get("freezeTimer").getAsInt()==87,"same client build preserves authored local values");
        check(ConfigFiles.load(client,schema).get("example:nested/mode").get("rules.freezeTimer").equals(81),"same client build preserves local mode overrides");
        ConfigFiles.prepareClient(client,schema,"test-2");
        check(ConfigFiles.read(client.resolve("rules.json5")).get("freezeTimer").getAsInt()==60,"version change regenerates defaults");
        write(client,"rules.json5","{freezeTimer:87}");
        write(client,".schema","test-2\nwrong-schema\n");
        ConfigFiles.prepareClient(client,schema,"test-2");
        check(ConfigFiles.read(client.resolve("rules.json5")).get("freezeTimer").getAsInt()==60,"schema change regenerates without a version bump");
        write(client,"rules.json5","{freezeTimer:87}");
        Files.delete(client.resolve(".schema"));
        ConfigFiles.prepareClient(client,schema,"test-2");
        check(ConfigFiles.read(client.resolve("rules.json5")).get("freezeTimer").getAsInt()==60,"missing reset marker retries regeneration");
        byte[] raw;
        try(var gzip=new GZIPInputStream(new ByteArrayInputStream(bundle.compressed()))){raw=gzip.readAllBytes();}
        var envelope=JsonParser.parseString(new String(raw,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
        envelope.addProperty("schema","wrong-schema");
        byte[] changed=ConfigSchema.JSON.toJson(envelope).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        rejects(() -> ConfigBundle.decode(compress(changed),ConfigSchema.hash(changed),schema),"schema mismatch");
        byte[] oversized=new byte[ConfigBundle.MAX_BYTES+1];
        rejects(() -> ConfigBundle.decode(compress(oversized),ConfigSchema.hash(oversized),schema),"decompression limit");
        System.out.println("CONFIG_SYSTEM_TEST_PASSED " + checks);
    }
    private static void compilerRejects(String declarations, String reason) throws Exception {
        Path directory = Files.createTempDirectory("wyspia-processor-check");
        Path source = directory.resolve("Fixture.java");
        Files.writeString(source, "package fixtures; import org.cat.express.wyspiaexpress.config.ConfigModel; public class Fixture { " + declarations + " }");
        var compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        var diagnostics = new javax.tools.DiagnosticCollector<javax.tools.JavaFileObject>();
        try (var manager = compiler.getStandardFileManager(diagnostics, null, java.nio.charset.StandardCharsets.UTF_8)) {
            var options = List.of("--release", "21", "-classpath", System.getProperty("java.class.path"),
                    "-processorpath", System.getProperty("wyspia.configProcessorPath"), "-d", directory.toString(), "-s", directory.toString());
            boolean compiled = compiler.getTask(null, manager, diagnostics, options, null, manager.getJavaFileObjects(source)).call();
            check(!compiled && diagnostics.getDiagnostics().stream().anyMatch(d -> d.getMessage(Locale.ROOT).contains(reason)), "processor rejects " + reason);
        }
    }

    private static byte[] compress(byte[] raw) {
        try {
            var out=new ByteArrayOutputStream();
            try(var gzip=new GZIPOutputStream(out)){gzip.write(raw);}
            return out.toByteArray();
        } catch(IOException e){throw new AssertionError(e);}
    }
}

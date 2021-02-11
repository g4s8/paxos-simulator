package wtf.g4s8.examples.configuration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.SneakyThrows;

import java.io.File;

public class Main {

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private static final String defaultCfg = "./src/main/resources/default_cfg.yml";
    private static String filePath;
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            filePath = args[0];
        } else {
            filePath = defaultCfg;
        }
        new TransactionTest().test();
    }

    @SneakyThrows
    public static Config initConfig() {
        return mapper.readValue(new File(filePath), Config.ConfigBuilder.class).build();
    }
}

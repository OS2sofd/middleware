package dk.sofd.opus.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class PersistentMapService {

	@SneakyThrows
	public Map<Object, Object> get(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return new HashMap<>();
		}
		else {
			Properties propertyMap = new Properties();
			propertyMap.load(new FileInputStream(file));
			return new HashMap<>(propertyMap);
		}
	}

	@SneakyThrows
	public void save(String filePath, Map<Object, Object> map) {
		Properties propertyMap = new Properties();
		propertyMap.putAll(map);
		propertyMap.store(new FileOutputStream(filePath), null);
	}
}

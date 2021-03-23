package net.etfbl.application;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.logging.*;



public class FileWatcher extends Thread {
	static public String file = "zarazeni.txt";
	BrojacZarazenih app;

	FileWatcher(BrojacZarazenih app) {
		this.app = app;
	}

	@Override
	public void run()

	{
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path dir = Paths.get("./Zarazeni");
			dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

			while (true) {

				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException ex) {
					return;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path fileName = ev.context();
					if (fileName.toString().equals(file) && kind.equals(ENTRY_MODIFY)) {
						try {
							sleep(100);
						} catch (Exception e) {
							Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, e.fillInStackTrace().toString());
						}

						List<String> content = Files.readAllLines(dir.resolve(fileName));

						app.promjeniBrojaZarazenih(content.get(0), content.get(1));

					}
				}

				boolean valid = key.reset();
				if (!valid) {
					break;
				}

			}

		} catch (IOException ex) {
			Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, ex.fillInStackTrace().toString());
		}
	}

}

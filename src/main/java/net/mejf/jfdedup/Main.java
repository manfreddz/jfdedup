package net.mejf.jfdedup;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class Main {

	private static Options options;
	private static boolean debug = false;

	static {
		options = new Options();

		options.addOption("d", "debug", false, "Print debug info");
		options.addOption("h", "help", false, "Print help");
		options.addOption("L", "make-hardlinks", true, "Whether to replace dups with hardlinks");
		options.addOption("D", "delete-dups", true, "Whether to delete dups");
	}

	public static void main(String[] args) {
		try {
			doRun(args);

		} catch (Exception e) {
			err("Exception: " + e.getLocalizedMessage());
			help();
		}
	}

	private static void doRun(String[] args) throws Exception {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption('h')) {
			help();
			return;
		}

		if (cmd.hasOption('d')) {
			Main.debug = true;
		}

		if (cmd.getArgList().isEmpty()) {
			throw new RuntimeException("No files or directories provided");
		}

		run(cmd);
	}

	private static void run(CommandLine cmd) {
		List<File> fileList = parseArgumentList(cmd);

		EntrySet entryList = scanForFiles(fileList);

		log("Sorting files");
		entryList.sortLargestFirst();

		eliminate("inodes with zero size", entryList, () -> Eliminations.eliminateWithZeroSize(entryList));

		eliminate("inodes with unique size", entryList, () -> Eliminations.eliminateWithUniqueSize(entryList));

		eliminate("inodes with unique start of file", entryList, () -> Eliminations.eliminateWithUniqueStartOfFile(entryList));

		eliminate("inodes with unique end of file", entryList, () -> Eliminations.eliminateWithUniqueEndOfFile(entryList));

		eliminate("files with unique hash", entryList, () -> Eliminations.eliminateWithUniqueHash(entryList));

		// TODO: Eliminate on all entry-data available
	}

	private static void eliminate(String eliminating, EntrySet entryList, Runnable r) {
		final int sizeBefore = entryList.size();
		log("Eliminating %s", eliminating);
		r.run();
		final int sizeAfter = entryList.size();
		log("Eliminated %d files, %d left", (sizeBefore - sizeAfter), sizeAfter);
	}

	private static List<File> parseArgumentList(CommandLine cmd) {
		log("Parsing argument list");
		List<File> fileList = getFileList(cmd);
		log("Found %d arguments", fileList.size());
		return fileList;
	}

	private static EntrySet scanForFiles(List<File> fileList) {
		log("Scanning for files");
		int prio = 0;
		EntrySet entryList = new EntrySet();
		for (File file : fileList) {
			Entry.construct(file, prio++, entryList);
		}
		log("Found %d files", entryList.size());
		return entryList;
	}

	private static List<File> getFileList(CommandLine cmd) {
		return cmd.getArgList().stream()
				.map(s -> new File(s))
				.peek(file -> {
					if (!file.exists()) {
						throw new RuntimeException(file.getPath() + " does not exist");

					} else if (!file.isFile() && !file.isDirectory()) {
						throw new RuntimeException(file.getPath() + " is not a file or directory");

					} else if (!file.canRead()) {
						throw new RuntimeException(file.getPath() + " is not readable");
					}
				})
				.collect(Collectors.toList());
	}

	private static void help() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("jfdedup [OPTION]... [FILE/DIRECTORY]...", options);
	}

	public static void debug(String format, Object... args) {
		if (debug) {
			System.out.printf("DBG: " + format + "\n", args);
		}
	}

	public static void log(String format, Object... args) {
		System.out.printf(format + "\n", args);
	}

	public static void err(String format, Object... args) {
		System.err.printf(format + "\n", args);
	}

	public static void progress(String format, Object... args) {
		System.out.printf(format + "\r", args);
	}

	static long lastProgress = 0;
	public static void progress(long done, long max) {
		long now = System.currentTimeMillis();
		if (now - lastProgress > 250) {
			lastProgress = now;
			System.out.printf("%d of %d (%.2f%%) done\r", done, max, done * 100.0 / max);
		}
	}
}

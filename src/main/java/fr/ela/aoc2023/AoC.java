package fr.ela.aoc2023;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AoC {

    public abstract void run();

    public static void repeat(int times, IntConsumer action) {
        IntStream.range(0, times).forEach(action);
    }

    private static String getDirectoryName(Class clazz) {
        return clazz.getSimpleName().toLowerCase();
    }

    private static String getFileName(String name) {
        String fileName = name;
        if (Boolean.parseBoolean(System.getProperty("test", "false"))) {
            fileName = fileName.concat("-test");
        }
        return fileName.concat(".txt");
    }

    public Path getPath(String name) {
        return Paths.get("target", "classes", getDirectoryName(getClass()), getFileName(name));
    }

    public Path getTestInputPath() {
        return getPath("input-test");
    }

    public Path getInputPath() {
        return getPath("input");
    }

    public String readFile(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }


    public List<List<String>> splitOnEmptyLines(Path path) {
        List<List<String>> result = new ArrayList<>();
        List<String> current = new ArrayList<>();
        result.add(current);
        for (String line : list(path)) {
            if (line.isEmpty()) {
                current = new ArrayList<>();
                result.add(current);
            } else {
                current.add(line);
            }
        }
        return result;
    }


    public <T> Stream<T> oneLineStream(Path path, String sep, Function<String, T> mapper) {
        return Arrays.stream(readFile(path).split(sep)).map(mapper);
    }

    public <T> List<T> oneLineList(Path path, String sep, Function<String, T> mapper) {
        return Arrays.stream(readFile(path).split(sep)).map(mapper).collect(Collectors.toList());
    }

    public <T> List<T> list(Path path, Function<String, T> mapper) {
        return stream(path, mapper).collect(Collectors.toList());
    }

    public Stream<String> stream(Path path) {
        try {
            return Files.lines(path);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public List<String> list(Path path) {
        return stream(path).collect(Collectors.toList());
    }

    public <T> Stream<T> stream(Path path, Function<String, T> mapper) {
        try {
            return Files.lines(path).map(mapper);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    boolean inRange(int i, int low, int high) {
        return i >= low && i < high;
    }

    public static void main(String[] args) {
        try {
            if (args.length == 0 || "last".equals(args[0])) {
                Path path = Path.of("src/main/java", AoC.class.getPackageName().split("\\."));
                String name = Files.list(path).map(Path::getFileName).map(Path::toString).filter(p -> p.endsWith(".java")).max(Comparator.naturalOrder()).orElse("");
                if (name.endsWith(".java")) {
                    name = name.substring(0, name.length() - ".java".length());
                }
                run(name);
            } else if (args.length == 1 && "all".equals(args[0])) {
                Path path = Path.of("src/main/java", AoC.class.getPackageName().split("\\."));
                Files.list(path).map(Path::getFileName).map(Path::toString)
                        .filter(p -> ! p.endsWith(AoC.class.getSimpleName()+".java"))
                        .filter(p -> p.endsWith(".java"))
                        .map(name -> name.substring(0, name.length() - ".java".length()))
                        .sorted(Comparator.naturalOrder()).forEach(AoC::run);
            } else {
                Arrays.stream(args).forEach(AoC::run);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void run(String className) {
        try {
            Class<AoC> clazz = (Class<AoC>) Class.forName(AoC.class.getPackageName() + "." + className);
            AoC instance = clazz.getDeclaredConstructor().newInstance();
            System.out.println("---- AoC "+clazz.getSimpleName()+" -----------");
            instance.run();
            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static IntStream reverseRange(int start, int end) {
        return IntStream.range(start, end).map(i -> end - i - 1);
    }

}

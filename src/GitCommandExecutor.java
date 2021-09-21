import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GitCommandExecutor {

    public static final Path PATH = Paths.get(System.getProperties().get("user.home") + "/my/cpython");

    public static void main(String[] args) throws IOException, InterruptedException {

        final Scanner scanner = new Scanner(System.in);
        final String input = scanner.nextLine();

        if (input != null) {
            final String[] commands = new String[4];
            final String[] cli = input.split("\s+");
            if (cli.length > 0) {
                System.arraycopy(cli, 0, commands, 0, cli.length);
                // git-bisect 7f777ed95a19224294949e1b4ce56bbffcb1fe9f v0.9.8 'python Lib/bisect.py'
                final String cmd = commands[0];
                // первый коммит
                String arg2 = commands[1];
                // верхний коммит
                String arg3 = commands[2];
                // команда для проверки
                String arg4 = commands[3];

                if (cmd.equals("git-bisect")) {
                    /* Первый коммит */
                    if (arg2 == null) {
                        // git rev-list --max-parents=0 main
                        final Process getFirstCommitProcess = buildProcess("git", "rev-list", "--max-parents=0", "main").start();
                        getFirstCommitProcess.waitFor();
                        arg2 = readOutputAsString(getFirstCommitProcess);
                        System.out.println("Второй коммит с начала истории: ");
                        System.out.print(arg2);
                        System.out.println();
                    }

                    try {
                        /* Собираем все коммиты в диапазоне */
                        // git log --pretty=format:%H $(git rev-list --max-parents=0 main)..v0.9.8
                        System.out.println("Запускаем поиск коммитов от начала до первого тега..");
                        final Process process = buildProcess(
                            "git",
                            "log",
                            "--pretty=format:%H",
                            arg2 + ".." + Optional.ofNullable(arg3).orElse("v0.9.8")
                        ).start();
                        final List<String> commandOutput = readOutputAsLines(process);
                        process.waitFor();

                        final String commit = searchRight(
                            commandOutput,
                            Optional.ofNullable(arg4).orElse("python Lib/bisect.py"),
                            predicate
                        );
                        System.out.print("Искомый коммит: " + commit);

                        /* Возвращаем репозиторий в приличное состояние */
                        buildProcess("git", "checkout", "-f", "main").start().waitFor();

                    } catch (IOException e) {
                        System.out.println(e);
                    }
                }
            }

        }
    }

    private static ProcessBuilder buildProcess(String... commands) throws IOException {
        return new ProcessBuilder(commands)
            .redirectErrorStream(true)
            .directory(PATH.toFile());
    }

    private static List<String> readOutputAsLines(Process process) throws IOException {
        try (final BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return output.lines().collect(Collectors.toList());
        }
    }

    private static String readOutputAsString(Process process) throws IOException {
        try (final BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return output.lines().collect(Collectors.joining("\n"));
        }
    }

    private static final BiFunction<String, String, Boolean> predicate = (commandFn, commit) -> {
        try {
            System.out.println("Смотрим " + commit + "...");
            System.out.println();
            final Process checkoutProcess = buildProcess(("git checkout -f " + commit).split(" ")).start();
            checkoutProcess.waitFor();

            final Process testPythonProcess = buildProcess(commandFn.split(" ")).start();
            String pythonStdout = readOutputAsString(testPythonProcess);
            testPythonProcess.waitFor();

            if (pythonStdout.contains("[Errno 2] No such file or directory")) {
                return false;
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return true;
    };


    private static String searchRight(List<String> range, String predicateFn, BiFunction<String, String, Boolean> predicate) {
        int le = 0;
        int ri = range.size();

        while (ri != le + 1) {
            int mid = (le + ri) / 2;
            if (predicate.apply(predicateFn, range.get(mid))) {
                le = mid;
            } else {
                ri = mid;
            }
        }

        if (le == range.size()) {
            return null;
        }

        return range.get(le);
    }

}

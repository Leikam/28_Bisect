import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GitCommandExecutor {

    public static final String HOME = (String) System.getProperties().get("user.home");
    public static final String CPYTHON = "my/cpython";


    public static void main(String[] args) throws IOException, InterruptedException {
        final Path path = Paths.get(HOME + "/" + CPYTHON);

        final String get_first_commit = "git rev-list --max-parents=0 main";
        final String get_branches = "git branch";
        final String get_ls = "ls";
        final String get_commits_before_first_tag = "git log --pretty=format:%H 7f777ed95a19224294949e1b4ce56bbffcb1fe9f..v0.9.8";


        try {
            final Process process = buildProcess(get_commits_before_first_tag, path).start();
            final List<String> commandOutput = getCommandOtput(process);
            process.waitFor();
            final String commit = bisec(commandOutput);
            System.out.print("commit = " + commit);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static ProcessBuilder buildProcess(String cmd, Path path) throws IOException {
        return new ProcessBuilder(cmd.split(" ")).directory(path.toFile());
    }

    private static List<String> getCommandOtput(Process process) throws IOException {
        try (final BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return output.lines().collect(Collectors.toList());
        }
    }


    private static final Function<String, Boolean> predicate = commit -> {
        return true;
    };

    private static String bisec(List<String> commits) {
        return searchRight(commits, predicate);
    }


    private static String searchRight(List<String> range, Function<String, Boolean> predicate) {
        int le = 0;
        int ri = range.size();

        while (ri != le + 1) {
            int mid = (le + ri) / 2;
            if (predicate.apply(range.get(mid))) {
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

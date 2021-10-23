package dev.liambloom.softwareEngineering.circus;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Employee(String lastname, String firstname, char middleInitial, String idNum, String category, String title) {
    public static final Pattern LN_PATTERN = Pattern.compile("[^,\\s]+");
    public static final Pattern LN_FN_DELIM_PATTERN = Pattern.compile(",\\s*|\\s+");
    public static final Pattern FN_PATTERN = Pattern.compile("[^.\\s]+");
    public static final Pattern FN_MI_DELIM_PATTERN = Pattern.compile("\\.?\\s+");
    public static final Pattern NON_SPACES_PATTERN = Pattern.compile("\\S+");
    public static final Pattern SPACES_PATTERN = Pattern.compile("\\s+");
    public static final Pattern REST_PATTERN = Pattern.compile(".*\\S");
    public static final Pattern ID_NUM_PATTERN = Pattern.compile("(?:\\d{3}-){2}\\d{3,4}");
    public static final Pattern NEXT_LINE = Pattern.compile(".*\\r?\\n?");
    /* Reading is hard because:
       - 3 people have a comma between their last and first name
       - 2 of those commas are followed by spaces
       - 2 people have dots after their first name
       - 6 people have dots after their middle initial
       - 1 person has a 10 digit in their id number (as opposed to everyone else's 9)
       - The entire class of 2013, plus one other guy, have full middle names (as opposed to initials)
       - 9 People have spaces in their titles
       - 2 id numbers are repeated
     */

    private static final String[] headers = { "Name", "Id", "Category", "Title" };

    public Employee {
        if (idNum != null && !ID_NUM_PATTERN.matcher(idNum).matches())
            throw new IllegalArgumentException("Invalid id num format");
        if (category != null)
            category = category.intern();
    }

    public Employee(Scanner s) {
        this(
                s.skip(LN_PATTERN).match().group(),
                s.skip(LN_FN_DELIM_PATTERN).skip(FN_PATTERN).match().group(),
                s.skip(FN_MI_DELIM_PATTERN).skip(NON_SPACES_PATTERN).match().group().charAt(0),
                s.skip(SPACES_PATTERN).skip(ID_NUM_PATTERN).match().group(),
                s.skip(SPACES_PATTERN).skip(NON_SPACES_PATTERN).match().group(),
                s.skip(SPACES_PATTERN).skip(REST_PATTERN).match().group()
        );
        s.skip(NEXT_LINE);
    }

    public String fullname() {
        return fullname(lastname, firstname, middleInitial);
    }

    public static String fullname(String lastname, String firstname, char middleInitial) {
        return lastname + ", " + firstname + (middleInitial == '#' ? "" : " " + middleInitial + ".");
    }

    public static void printAll(Stream<Employee> allEmployees, boolean indexed) {
        final String[][] employeeData = allEmployees
                .map(e -> new String[]{ e.fullname(), e.idNum(), e.category(), e.title() })
                .toArray(String[][]::new);
        final int[] columnWidths = stringLengths(headers);

        for (String[] e : employeeData) {
            final int[] employeeWidths = stringLengths(e);
            for (int i = 0; i < columnWidths.length; i++) {
                if (columnWidths[i] < employeeWidths[i])
                    columnWidths[i] = employeeWidths[i];
            }
        }

        String fmt = Arrays.stream(columnWidths)
                .mapToObj(w -> " %-" + w + "s ")
                .collect(Collectors.joining("|"))
                + "%n";
        int indexPadWidth = (int) Math.ceil(Math.log10(columnWidths.length));
        String indexPad = " ".repeat(indexPadWidth + 1);

        if (indexed)
            System.out.print(indexPad);
        System.out.printf(fmt, (Object[]) headers);
        if (indexed)
            System.out.print(indexPad);
        System.out.println(Arrays.stream(columnWidths)
                .mapToObj(w -> "-".repeat(w + 2))
                .collect(Collectors.joining("+")));
        for (int i = 0; i < employeeData.length; i++) {
            if (indexed)
                System.out.printf("%" + indexPadWidth + "d ", i + 1);
            System.out.printf(fmt, (Object[]) employeeData[i]);
        }
    }

    private static int[] stringLengths(String... strs) {
        return Arrays.stream(strs).mapToInt(String::length).toArray();
    }

    public static final Comparator<Employee> nameComparator = Comparator.comparing(Employee::lastname)
            .thenComparing(Employee::firstname)
            .thenComparing(Employee::middleInitial);
}

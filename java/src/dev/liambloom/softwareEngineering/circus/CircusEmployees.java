package dev.liambloom.softwareEngineering.circus;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import dev.liambloom.softwareEngineering.chapter6.Ask;

public class CircusEmployees {
    public static final String EMPLOYEES_FILE = "employees.txt";
    private static final Pattern NEXT_LINES = Pattern.compile("(?:\\s*\\r?\\n?)*");

    private final SortedMap<String, NavigableSet<Employee>> categoryMap = new TreeMap<>();
    private final SortedMap<String, Employee> idMap = new TreeMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        CircusEmployees circus = new CircusEmployees(new File(EMPLOYEES_FILE));

        while (true) {
            System.out.println("""
                Menu:
                    (1) Print list alphabetically
                    (2) Print list by IdNum
                    (3) Insert new Employee
                    (4)	Delete an Employee
                    (5)	Print only a particular category list of employees alphabetically
                    (6)	Print entire list of all employees by category alphabetically
                    (7)	Add a category
                    (8)	Delete a category (and ALL corresponding employees)
                    (9)	Quit""");
            switch (Ask.forInt("Selection", 1, 9, "That's not a valid menu option")) {
                case 1:
                    circus.printAlphabetically();
                    break;
                case 2:
                    circus.printByIdNum();
                    break;
                case 3:
                    circus.insertEmployee();
                    break;
                case 4:
                    circus.deleteEmployee();
                    break;
                case 5:
                    circus.printCategory();
                    break;
                case 6:
                    circus.printAllCategorically();
                    break;
                case 7:
                    circus.addCategory();
                    break;
                case 8:
                    circus.deleteCategory();
                    break;
                case 9:
                    System.out.println("Thanks for visiting!");
                    return;
            }
        }
    }

    public CircusEmployees(File employees) throws FileNotFoundException {
        Scanner s = new Scanner(employees);
        while (s.hasNext()) {
            Employee newEmployee = new Employee(s);
            insertEmployeeInternal(newEmployee);
            s.skip(NEXT_LINES);
        }
    }

    public void printAlphabetically() {
        PriorityQueue<PeekableIterator<Employee>> pq =
                new PriorityQueue<>(Comparator.comparing(PeekableIterator::peek, Employee.nameComparator));

        for (SortedSet<Employee> e : categoryMap.values()){
            if (!e.isEmpty())
                pq.add(PeekableIterator.from(e.iterator()));
        }

        List<Employee> list = new LinkedList<>();

        while (!pq.isEmpty()) {
            PeekableIterator<Employee> iter = pq.poll();
            list.add(iter.next());
            if (iter.hasNext())
                pq.add(iter);
        }

        Employee.printAll(list.stream(), false);
    }

    public void printByIdNum() {
        Employee.printAll(idMap.values().stream(), false);
    }

    public void insertEmployee() {
        insertEmployeeInternal(new Employee(
                Ask.forString("Last Name"),
                Ask.forString("First Name"),
                Ask.forChar("Middle Initial ('#' if none)"),
                ((Supplier<String>) () -> {
                    while (true) {
                        String s = Ask.forString("Id Number");
                        if (!Employee.ID_NUM_PATTERN.matcher(s).matches())
                            System.out.println("Invalid pattern");
                        else if (idMap.containsKey(s))
                            System.out.println("Duplicate Id number not allowed");
                        else
                            return s;
                    }
                }).get(),
                Ask.forString("Category"),
                Ask.forString("Title")
        ));
        System.out.println("Employee inserted");
    }

    public void insertEmployeeInternal(Employee e) {
        categoryMap.computeIfAbsent(e.category(),
                k -> new TreeSet<>(Employee.nameComparator))
                .add(e);
        idMap.compute(e.idNum(), (k, v) -> {
            if (v == null)
                return e;
            else
                throw new IllegalArgumentException("Duplicate id " + e.idNum());
        });
    }

    public void deleteEmployee() {
        Employee dummy = new Employee(
                Ask.forString("Last Name"),
                Ask.forString("First Name"),
                Ask.forChar("Middle Initial"),
                null, null, null);
        List<Employee> matches = new LinkedList<>();

        for (NavigableSet<Employee> c : categoryMap.values())
            matches.addAll(c.subSet(dummy, true, dummy, true));

        Employee match;
        if (matches.isEmpty()) {
            System.out.printf("Employee %s does not exist%n", dummy.fullname());
            return;
        }
        else if (matches.size() > 1) {
            System.out.printf("There are multiple employees with the name %s%n%n", dummy.fullname());
            System.out.println();
            Employee.printAll(matches.stream(), true);
            System.out.println();
            match = matches.get(Ask.forInt("Employee to delete", 1, matches.size()) - 1);
        }
        else
            match = matches.get(0);

        categoryMap.get(match.category()).remove(match);
        idMap.remove(match.idNum());
        System.out.printf("Employee %s has been removed%n", dummy.fullname());
    }

    public void printCategory() {
        final String category = Ask.forString("Category to print");
        Set<Employee> employees = categoryMap.get(category);

        if (employees == null)
            System.out.printf("Category %s doesn't exists%n", category);
        else {
            Employee.printAll(employees.stream(), false);
        }
    }

    public void printAllCategorically() {
        Employee.printAll(categoryMap.values().stream().flatMap(Collection::stream), false);
    }

    public void addCategory() {
        categoryMap.compute(Ask.forString("Category to create"), (k, v) -> {
            if (v == null){
                v = new TreeSet<>(Employee.nameComparator);
                System.out.println("Category created");
            }
            else
                System.out.printf(
                        "Category %s already exists%n", k);
            return v;
        });
    }

    public void deleteCategory() {
        final String category = Ask.forString("Category to delete");
        Set<Employee> categorySet = categoryMap.remove(category);
        if (categorySet == null)
            System.out.println("Category " + category + " doesn't exist");
        else {
            for (Employee e : categorySet)
                idMap.remove(e.idNum());
            System.out.println("Category " + category + " has been deleted");
        }
    }
}

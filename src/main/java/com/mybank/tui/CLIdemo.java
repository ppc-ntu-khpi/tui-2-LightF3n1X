package com.mybank.tui;

import com.mybank.tui.data.DataSource;
import com.mybank.tui.domain.*;
import com.mybank.tui.reporting.CustomerReport;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.*;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;


/**
 * Sample application to show how jLine can be used.
 *
 * @author sandarenu
 *
 */
/**
 * Console client for 'Banking' example
 *
 * @author Alexander 'Taurus' Babich
 */
public class CLIdemo {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static final CustomerReport customerReport = new CustomerReport();
    private static final DataSource dataSource = new DataSource("src/main/java/com/mybank/tui/data/test.dat");
    private String[] commandsList;

    CLIdemo() {
        System.setProperty("org.jline.terminal.dumb", "true");
    }
    public void init(){
        commandsList = new String[]{"help", "customers", "customer", "exit"};
        try {
            dataSource.loadData();
        }catch (IOException e) {
            System.out.println("File not exists.");
        }
    }

    public void run() {
        AnsiConsole.systemInstall(); // needed to support ansi on Windows cmd
        printWelcomeMessage();
        LineReaderBuilder readerBuilder = LineReaderBuilder.builder();
        List<Completer> completers = new LinkedList<Completer>();

        completers.add(new StringsCompleter(commandsList));
        readerBuilder.completer(new ArgumentCompleter(completers));

        LineReader reader = readerBuilder.build();

        String line;
        PrintWriter out = new PrintWriter(System.out);

        while ((line = readLine(reader, "")) != null) {
            if ("help".equals(line)) {
                printHelp();
            } else if ("customers".equals(line)) {
                AttributedStringBuilder a = new AttributedStringBuilder()
                        .append("\nThis is all of your ")
                        .append("customers", AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                        .append(":");

                System.out.println(a.toAnsi());
                if (Bank.getNumberOfCustomers() > 0) {
                    System.out.println("\nLast name\tFirst Name\tBalance");
                    System.out.println("---------------------------------------");
                    for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
                        System.out.println(Bank.getCustomer(i).getLastName() + "\t\t" + Bank.getCustomer(i).getFirstName() + "\t\t$" + Bank.getCustomer(i).getAccount(0).getBalance());
                    }
                } else {
                    System.out.println(ANSI_RED+"Your bank has no customers!"+ANSI_RESET);
                }

            } else if (line.indexOf("customer") != -1) {
                try {
                    int custNo = 0;
                    if (line.length() > 8) {
                        String strNum = line.split(" ")[1];
                        if (strNum != null) {
                            custNo = Integer.parseInt(strNum);
                        }
                    }
                    Customer cust = Bank.getCustomer(custNo);
                    String accType = cust.getAccount(0) instanceof CheckingAccount ? "Checkinh" : "Savings";

                    AttributedStringBuilder a = new AttributedStringBuilder()
                            .append("\nThis is detailed information about customer #")
                            .append(Integer.toString(custNo), AttributedStyle.BOLD.foreground(AttributedStyle.RED))
                            .append("!");

                    System.out.println(a.toAnsi());

                    System.out.println("\nLast name\tFirst Name\tAccount Type\tBalance");
                    System.out.println("-------------------------------------------------------");
                    System.out.println(cust.getLastName() + "\t\t" + cust.getFirstName() + "\t\t" + accType + "\t$" + cust.getAccount(0).getBalance());
                } catch (Exception e) {
                    System.out
                            .println(ANSI_RED + "ERROR! Wrong customer number!" + ANSI_RESET);
                }
            } else if("report".equals(line)) {
                customerReport.generateReport();
            } else if ("exit".equals(line)) {
                System.out.println("Exiting application");
                return;
            } else {
                System.out
                        .println(ANSI_RED + "Invalid command, For assistance press TAB or type \"help\" then hit ENTER." + ANSI_RESET);
            }
        }

        AnsiConsole.systemUninstall();
    }

    private void printWelcomeMessage() {
        System.out
                .println("\nWelcome to " + ANSI_GREEN + " MyBank Console Client App" + ANSI_RESET + "! \nFor assistance press TAB or type \"help\" then hit ENTER.");

    }

    private void printHelp() {
        System.out.println("help\t\t\t- Show help");
        System.out.println("customer\t\t- Show list of customers");
        System.out.println("customer \'index\'\t- Show customer details");
        System.out.println("exit\t\t\t- Exit the app");
        System.out.println("report\t\t\t- show report");
    }

    private String readLine(LineReader reader, String promptMessage) {
        try {
            String line = reader.readLine(promptMessage + ANSI_YELLOW + "\nbank> " + ANSI_RESET);
            return line.trim();
        } catch (UserInterruptException e) {
            // e.g. ^C
            return null;
        } catch (EndOfFileException e) {
            // e.g. ^D
            return null;
        }
    }

    public static void main(String[] args) {
        CLIdemo shell = new CLIdemo();
        shell.init();
        shell.run();
    }
}
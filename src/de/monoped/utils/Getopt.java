package de.monoped.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author bernd.eggink@monoped.de
 */

public class Getopt {

    /**
     * Handles program arguments like Unix getopt(). Example: <pre>
     *  void main(String[] argv)
     *  {
     *      int     opt;
     *      boolean x = false;
     *      Getopt  getopt = new Getopt(argv, "a:hx");
     *      String  argA = "";
     * <p/>
     *      // get Options
     * <p/>
     *      while ((opt = getopt.getOption()) != -1)
     *      {
     *          switch (opt)
     *          {
     *              case 'a':   argA = getopt.getOptarg();
     *                          break;
     *              case 'h':   help();
     *                          break;
     *              case 'x':   x = true;
     *                          break;
     *              default:    System.out.println("wrong option: " + getOptopt());
     *          }
     *      }
     * <p/>
     *      // handle non-option parameters
     * <p/>
     *      String[]   files = getopt.getParms();
     * <p/>
     *      for (int i = 0; i < files.length; ++i)
     *          doSomethingWith(files[i]);
     *  }
     * <p/>
     *  If the first program is of the form "@filename", this file
     *  is read and each line is considered a program parameter.
     * <p/>
     *  Legal calls:
     *      java Mainclass -hx file1 file2
     *      java Mainclass -xa blurp -h -- file3
     * <p/>
     *  Illegal calls:
     *      java Mainclass -y f
     *      java Mainclass -a
     * <p/>
     *  The special argument -- denotes the end of the options. All Arguments
     *  after this will be considered as non-options, even if starting with -.
     * <p/>
     *  </pre>
     *
     **/


    private String[] argv;
    private String opts;
    private int ichr = 0, optind = 0;
    private char optopt;
    private boolean opterr = true;
    private String optarg;

    //----------------------------------------------------------------------

    /**
     * Constructs a Getopt object by reading all arguments from a file.
     *
     * @param filename Name of the file to read.
     * @param opts     The possible options.
     * @param opterr   If true, an error message will be printed if an illegal option character
     *                 is found.
     */

    public Getopt(String filename, String opts, boolean opterr)
            throws IOException {
        this(filename, opts);
        this.opterr = opterr;
    }

    //----------------------------------------------------------------------

    /**
     * Same as Getopt(filename, opts, true)
     */

    public Getopt(String filename, String opts)
            throws IOException {
        this.opts = opts;
        getArgsFromFile(filename);
    }

    //----------------------------------------------------------------------

    /**
     * Constructs a Getopt object using the main() parameter list.
     *
     * @param argv   The arguments of main()
     * @param opts   The possible options. Each option is a single character.
     *               If followed by a colon, the option given in the command line
     *               must be followed by an argument.
     * @param opterr If true, an error message will be printed if an illegal option character
     *               is encountered.
     */

    public Getopt(String[] argv, String opts, boolean opterr)
            throws IOException {
        this(argv, opts);
        this.opterr = opterr;
    }

    //----------------------------------------------------------------------

    /**
     * Same as Getopt(argv, opts, true).
     */

    public Getopt(String[] argv, String opts)
            throws IOException {
        this.opts = opts;

        if (argv.length == 0 || argv[0].length() == 0 || argv[0].charAt(0) != '@') {
            this.argv = argv;
            return;
        }

        getArgsFromFile(argv[0].substring(1));
    }

    //----------------------------------------------------------------------

    /**
     * Read args from file, one parameter per line
     */

    private void getArgsFromFile(String name)
            throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(name)));
        ArrayList<String> avec = new ArrayList<String>();
        String line;

        while ((line = reader.readLine()) != null)
            avec.add(line);

        this.argv = (String[]) avec.toArray(new String[0]);
        reader.close();
    }

    //----------------------------------------------------------------------

    /**
     * Returns the current argument or null.
     */

    public String getOptarg() {
        return optarg;
    }

    //----------------------------------------------------------------------

    /**
     * Returns the next option as int value,
     * -1 if no more options are available, '?' if the option is illegal.
     */

    public int getOption() {
        char c;
        int iopt;

        if (ichr == 0) {
            // beginning of word

            if (optind >= argv.length || argv[optind].length() == 0 || argv[optind].charAt(0) != '-')
                return -1;

            if (argv[optind].equals("-") || argv[optind].equals("--")) {
                ++optind;
                return -1;
            }
        }

        // had -

        c = argv[optind].charAt(++ichr);

        if (c == ':' || (iopt = opts.indexOf(c)) < 0) {
            if (opterr)
                System.err.println("+++ Illegal option: " + c);

            if (ichr + 1 >= argv[optind].length()) {
                ++optind;
                ichr = 0;
            }

            optopt = c;
            optarg = null;
            return '?';
        }

        if (iopt + 1 < opts.length() && opts.charAt(iopt + 1) == ':') {
            // must have optarg

            if (++ichr < argv[optind].length())
                optarg = argv[optind++].substring(ichr);
            else if (++optind >= argv.length) {
                if (opterr)
                    System.err.println("+++ Option " + c + " requires an argument");

                ichr = 0;
                optopt = c;
                return '?';
            } else
                optarg = argv[optind++];

            ichr = 0;
        } else {
            // no optarg

            if (ichr + 1 >= argv[optind].length()) {
                ++optind;
                ichr = 0;
            }

            optarg = null;
        }

        return c;
    }

    //----------------------------------------------------------------------

    /**
     * Returns the unrecognized option character.
     */

    public char getOptopt() {
        return optopt;
    }

    //----------------------------------------------------------------------

    /**
     * Returns parameters not yet handled by getOption() as array of Strings.
     */

    public String[] getParms() {
        String[] parms = new String[argv.length - optind];

        for (int i = 0; optind + i < argv.length; ++i)
            parms[i] = argv[optind + i];

        return parms;
    }

    //----------------------------------------------------------------------


}

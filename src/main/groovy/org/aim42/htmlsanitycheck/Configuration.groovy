package org.aim42.htmlsanitycheck

import org.aim42.filesystem.FileCollector

// see end-of-file for license information

/**
 * Handles (and can verify) configuration options.
 *
 * Implemented as REGISTRY pattern
 *
 * This class needs to be updated if additional configuration options are added.
 *
 * Ideas for additional config options:
 *
 * - verbosity level on console during checks
 *
 * - which HTTP status codes shall result in warnings, and which shall map to errors
 * - list of URLs to exclude from httpLinkchecks
 * - list of hosts to exclude from httpLinkChecks
 */

class Configuration {

    // the configuration registry instance
    private static Configuration internalRegistry


    /*****************************************
     * configuration item names
     *
     * NEVER use any string constants for configuration
     * item names within source code!
     ****************************************/

    // sourceDocuments is a collection of Strings, maybe only a single String
    final static String ITEM_NAME_sourceDocuments = "sourceDocuments"
    final static String ITEM_NAME_sourceDir = "sourceDir"

    final static String ITEM_NAME_checkingResultsDir = "checkingResultsDir"
    final static String ITEM_NAME_junitResultsDir = "junitResultsDir"

    final static String ITEM_NAME_consoleReport = "consoleReport"

    // e.g. for Gradle based builds: fail the build if errors are found in Html file(s)
    final static String ITEM_NAME_failOnErrors = "failOnErrors"

    final static String ITEM_NAME_httpConnectionTimeout = "httpConnectionTimeout"

    // currently unused - planned for future enhancements
    final static String ITEM_NAME_httpWarningStatusCodes = "httpWarningStatusCodes"
    final static String ITEM_NAME_httpErrorStatusCodes = "httpErrorStatusCodes"
    final static String ITEM_NAME_urlsToExclude = "urlsToExclude"
    final static String ITEM_NAME_hostsToExclude = "hostsToExclude"

    /***************************
     * private member
     **************************/
    private Map configurationItems = [:]


    // REGISTRY methods
    // ****************
    private static synchronized Configuration registry() {
        if (internalRegistry == null) {
            internalRegistry = new Configuration();
        }
        return internalRegistry;
    }

    // constructor to set (some) default values
    private Configuration() {

      this.configurationItems.put( ITEM_NAME_httpConnectionTimeout, 5000 )
    }


    /** retrieve a single configuration item
     *
     * @param itemName
     * @return
     */
    public static synchronized Object getConfigItemByName( final String itemName) {
        return registry().configurationItems.get(itemName)
    }

    // special HtmlSanityChecker methods for mandatory configuration items
    // *******************************************************************

    /**
     * convenience method for simplified testing
     */
    static synchronized void addSourceFileConfiguration(File srcDir, Set<String> srcDocs) {
        registry().addConfigurationItem(ITEM_NAME_sourceDir, srcDir)
        registry().addConfigurationItem(ITEM_NAME_sourceDocuments, srcDocs)
    }


    /**
     * @return true if item is already present, false otherwise
     */
    static boolean checkIfItemPresent(String itemName) {
        boolean result = false
        if (registry().configurationItems.get(itemName) != null) {
            result = true
        }
        return result
    }

    /**
     * @return the number of configuration items
     */
    static int nrOfConfigurationItems() {
        return registry().configurationItems.size()
    }

    /** add a single configuration item
     *
     * @param itemName
     * @param itemValue
     */
    static void addConfigurationItem(String itemName, Object itemValue) {
        registry().configurationItems.put(itemName, itemValue)
    }



    /**
     * checks plausibility of configuration:
     * We need at least one html file as input, maybe several
     * @param configuration instance
     *
     * srcDocs needs to be of type {@link org.gradle.api.file.FileCollection}
     * to be Gradle-compliant
     */
    public static Boolean isValid() {
        // prior to 1.0.0-RC-2: public static Boolean isValid(File srcDir, Set<String> srcDocs) {

        // we need at least srcDir and srcDocs!!
        File srcDir = registry().getConfigItemByName(Configuration.ITEM_NAME_sourceDir)
        Set<String> srcDocs = registry().getConfigItemByName(Configuration.ITEM_NAME_sourceDocuments)

        // cannot check if source director is null (= unspecified)
        if ((srcDir == null)) {
            throw new MisconfigurationException("source directory must not be null")
        }

        // cannot check if both input params are null
        if ((srcDir == null) && (srcDocs == null)) {
            throw new MisconfigurationException("both sourceDir and sourceDocs were null")
        }

        // no srcDir was given and empty SrcDocs
        if ((!srcDir) && (srcDocs != null)) {
            if ((srcDocs?.empty)) {
                throw new MisconfigurationException("both sourceDir and sourceDocs must not be empty")
            }
        }
        // non-existing srcDir is absurd too
        if ((!srcDir.exists())) {
            throw new MisconfigurationException("given sourceDir $srcDir does not exist.")
        }

        // if srcDir exists but is empty... no good :-(
        if ((srcDir.exists())
                && (srcDir.isDirectory())
                && (srcDir.directorySize() == 0)) {
            throw new MisconfigurationException("given sourceDir $srcDir is empty")
        }

        // if srcDir exists but does not contain any html file... no good
        if ((srcDir.exists())
                && (srcDir.isDirectory())
                && (FileCollector.getAllHtmlFilesFromDirectory(srcDir).size() == 0)) {
            throw new MisconfigurationException("no html file found in $srcDir")
        }

        // if no exception has been thrown until now,
        // the configuration seems to be valid..
        return true
    }


    @Override
    public static String toString() {
        return "Configuration{" +
                "configurationItems=" + registry().configurationItems +
                '}';
    }
}
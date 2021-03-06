package p3eapi;

import java.util.Hashtable;
import java.util.logging.Logger;

import com.primavera.integration.client.GlobalObjectManager;
import com.primavera.integration.client.EnterpriseLoadManager;
import com.primavera.integration.client.bo.BOIterator;
import com.primavera.integration.client.bo.object.Project;
import com.primavera.integration.client.bo.object.BaselineProject;
import com.primavera.ServerException;
import com.primavera.integration.network.NetworkException;
import com.primavera.integration.client.bo.BusinessObjectException;
import com.primavera.integration.client.Session;

/**
 * Holds all the information about how to connect to the Primavera API.
 *
 * @author Barrie Callender
 */
class P6Connection
{
  private static Logger logger = Logger.getLogger(P6Connection.class.getName());

  private Hashtable<String,Integer> modeMap = new Hashtable<String,Integer>();

  public static final String LOCAL_MODE = "local";
  public static final String STANDARD_MODE = "standard";
  public static final String SSL_MODE = "ssl";
  public static final String COMPRESSION_MODE = "compression";

  private IRmiUrl p6rmiurl = null;
  private ISession p6session = null;
  private boolean isLoggedIn = false;
  private int iCallingMode;
  private String sHostname;
  private int iPort;

  /**
   * Constructor establishes connection information based on the
   * P3eAPIExportApp.properties file and defaults
   */
  P6Connection(IRmiUrl rmiurl, ISession session)
  {
    super();

    this.p6rmiurl = rmiurl;
    this.p6session = session;
    this.isLoggedIn = false;

    // set the map up.
    modeMap.put(LOCAL_MODE,p6rmiurl.getLocalService());
    modeMap.put(STANDARD_MODE,p6rmiurl.getStandardRmiService());
    modeMap.put(SSL_MODE,p6rmiurl.getSSLRmiService());
    modeMap.put(COMPRESSION_MODE,p6rmiurl.getCompressionRmiService());

    // open the properties file and read in the variables

    Configuration props = new Configuration();

    logger.info("property mode=" + props.getProperty("mode",LOCAL_MODE));
    setCallingMode(props.getProperty("mode",LOCAL_MODE));
    setHostname(props.getProperty("hostname","localhost"));
    setPort(new Integer(props.getProperty("port","9099")) );

  }

  /**
   * Returns calling mode as either Local or Remote
   *
   * @return
   */
  public int getCallingMode() {
      return iCallingMode;
  }

  /**
   * Sets the calling mode.
   *
   * @param mode
   * @return
   */
  public int setCallingMode(String mode) {

    if (modeMap.containsKey(mode.toLowerCase())) {
      iCallingMode = (Integer)modeMap.get(mode.toLowerCase());
    }
    else {
      iCallingMode = (Integer)modeMap.get(STANDARD_MODE);
    }

    return getCallingMode();
  }

  /**
   * Returns the name of the host for non=local modes
   *
   * @return
   */
  public String getHostname() {
      return sHostname;
  }

  /**
   * Sets the name of the host for use in non-local modes
   *
   * @param Hostname
   * @return
   */
  public String setHostname(String Hostname) {
      sHostname = "http://" + Hostname;
      return getHostname();
  }

  /**
   * Returns the port number for non-local modes
   *
   * @return
   */
  public int getPort() {
      return iPort;
  }

  /**
   * Returns the port number for non-local modes.
   *
   * @param Port
   * @return
   */
  public int setPort(int Port) {
      iPort = Port;
      return getPort();
  }

  /**
   * Uses the information in this object to return a URL that primavera can use
   * to connect to the Primavera API
   *
   * @return
   */
  public String getRmiUrl() {

      return p6rmiurl.getRmiUrl(getCallingMode(),getHostname(),getPort());
  }

  public void login(String username, String password) {
	  String dbase = null;
	  boolean result = false;
	  try {
		  result = p6session.login(this.getRmiUrl(),dbase,username, password);
	  }
	  catch (Exception ex)
	  {
		  result = false;
	  }
	  setIsLoggedIn(result);
  }

  public IProject getProjectOrBaseline(String projectId) {
    IProject project = null;
    GlobalObjectManager gom = p6session.getGlobalObjectManager();
    BOIterator<Project> boiProject = null;

    try {
      boiProject = gom.loadProjects(new String[] { "ObjectId", "Id", "Name" },
          "Id = '" + projectId + "'", null );

    // Are we dealing with a project?
    if (boiProject.hasNext()) {
      project = new P6Project(boiProject.next());
    }
    else {
      EnterpriseLoadManager elm = p6session.getEnterpriseLoadManager();
      BOIterator <BaselineProject> boiBaseline = elm.loadBaselineProjects(new String[] { "ObjectId", "Id", "Name" },
          "Id = '" + projectId + "'", null );
      if (boiBaseline.hasNext()) {
        project = new P6Baseline(boiBaseline.next());
      }
    }
  }
  catch (ServerException ex) {
    logger.info("Primavera ServerException error determining project or baseline");
    logger.info(ex.toString());
  }
  catch (NetworkException ex) {
    logger.info("Primavera NetworkException error determining project or baseline");
    logger.info(ex.toString());
  }
  catch (BusinessObjectException ex) {
    logger.info("Primavera BusinessObjectException error determining project or baseline");
    logger.info(ex.toString());
  }
    return project;
  }

  public Session getSession() {
    return this.p6session.getSession();
  }

  private void setIsLoggedIn(boolean bool) {
	  this.isLoggedIn = bool;
  }

  public boolean isLoggedIn() {
	  return this.isLoggedIn;
  }


}

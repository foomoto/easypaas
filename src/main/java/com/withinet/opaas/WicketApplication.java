package com.withinet.opaas;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.lang.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.withinet.opaas.controller.RoleController;
import com.withinet.opaas.controller.UserController;
import com.withinet.opaas.controller.common.AccountLoginException;
import com.withinet.opaas.controller.common.RoleControllerException;
import static com.withinet.opaas.controller.common.ServiceProperties.*;
import com.withinet.opaas.model.PermissionRepository;
import com.withinet.opaas.model.RolePermissionRepository;
import com.withinet.opaas.model.RoleRepository;
import com.withinet.opaas.model.domain.Permission;
import com.withinet.opaas.model.domain.Role;
import com.withinet.opaas.model.domain.RolePermission;
import com.withinet.opaas.model.domain.User;
import com.withinet.opaas.wicket.Education;
import com.withinet.opaas.wicket.Index;
import com.withinet.opaas.wicket.html.AccountIndex;
import com.withinet.opaas.wicket.html.Authenticated;
import com.withinet.opaas.wicket.html.BundleIndex;
import com.withinet.opaas.wicket.html.Dashboard;
import com.withinet.opaas.wicket.html.InstanceIndex;
import com.withinet.opaas.wicket.html.Login;
import com.withinet.opaas.wicket.html.PageError;
import com.withinet.opaas.wicket.html.ProjectIndex;
import com.withinet.opaas.wicket.html.Register;
import com.withinet.opaas.wicket.html.TeamIndex;
import com.withinet.opaas.wicket.html.Users;
import com.withinet.opaas.wicket.services.UserSession;

/**
 * The web application class also serves as spring boot starting point by using
 * spring boot's EnableAutoConfiguration annotation and providing the main
 * method.
 * 
 * @author Folarin Omotoriogun
 * 
 */
@Component (value = "wicketapplication")
@EnableAutoConfiguration
@ComponentScan
public class WicketApplication extends WebApplication {

	@Autowired
	private ApplicationContext applicationContext;
	
	private final Map<Long, UserSession> sessions = new HashMap<Long, UserSession>();

	/**
	 * spring boot main method to build context
	 * 
	 * @param args
	 */
	public static void main(String... args) {
		PropertiesInstaller.install();
		SpringApplication.run(WicketApplication.class, args);

	}

	/**
	 * provides page for default request
	 */
	@Override
	public Class<? extends Page> getHomePage() {
		return Index.class;
	}

	@Autowired
	com.withinet.opaas.model.UserRepository userRepo;

	@Autowired
	PermissionRepository permissionRepo;

	@Autowired
	RoleRepository roleRepo;

	@Autowired
	RolePermissionRepository rpRepo;

	@Autowired
	RoleController roleCtrl;

	/**
	 * <ul>
	 * <li>making the wicket components injectable by activating the
	 * SpringComponentInjector</li>
	 * <li>mounting the test page</li>
	 * <li>logging spring service method output to showcase working integration</li>
	 * </ul>
	 */
	@Override
	protected void init() {
		super.init();
		
		getApplicationSettings().setPageExpiredErrorPage(PageError.class);
		getApplicationSettings().setAccessDeniedPage(PageError.class);
		getApplicationSettings().setInternalErrorPage(PageError.class);
		getJavaScriptLibrarySettings().setJQueryReference(
				new PackageResourceReference(Authenticated.class,
						"../js/libs/jquery-1.9.1.min-ver-1409112303268.js"));
		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);
		getApplicationSettings().setUploadProgressUpdatesEnabled(true);
		getRequestCycleSettings().setRenderStrategy(
				IRequestCycleSettings.RenderStrategy.ONE_PASS_RENDER);
		getMarkupSettings().setStripWicketTags(true);
		getMarkupSettings().setStripComments(true);
		getMarkupSettings().setCompressWhitespace(true);
		getApplicationSettings().setUploadProgressUpdatesEnabled(true);
		getStoreSettings().setMaxSizePerSession(Bytes.kilobytes(5000));
		getStoreSettings().setInmemoryCacheSize(500);
		getComponentInstantiationListeners().add(
				new SpringComponentInjector(this, applicationContext));
		mountPage("/login", Login.class);
		mountPage("/register", Register.class);
		mountPage("/projects", ProjectIndex.class);
		mountPage("/dashboard", Dashboard.class);
		mountPage("/bundles", BundleIndex.class);
		mountPage("/team", TeamIndex.class);
		mountPage("/instances", InstanceIndex.class);
		mountPage("/account", AccountIndex.class);
		mountPage("/education", Education.class);
		mountPage ("/users", Users.class);

		if (userRepo.findByRole("SUPER ADMINISTRATOR").size() == 0) {
			User web = new User();
			web.setCreated(new Date());
			web.setFullName(SUPER_ADMIN_REAL_NAME);
			web.setPlatformName("OSGi Cloud Platform as a Service");
			web.setStatus("Active");
			web.setEmail(SUPER_ADMIN_EMAIL);
			web.setPassword(SUPER_ADMIN_PASSWORD);
			web.setLocation("United Kingdom");
			web.setAdministrator(web);
			web.setQuota(-1);
			web.setRole("SUPER ADMINISTRATOR");
			web.setWorkingDirectory("");
			web.setIntroduction("I am the main adminitrator for this system");
			userRepo.save(web);

			Permission p1 = new Permission();
			p1.setValue("readProject");
			p1.setDescription("Project: User can read project information including bundles");
			permissionRepo.save(p1);

			Permission p2 = new Permission();
			p2.setValue("createProject");
			p2.setDescription("Project: User can create new projects");
			permissionRepo.save(p2);

			Permission p3 = new Permission();
			p3.setValue("deleteProject");
			p3.setDescription("Project: User can delete created projects");
			permissionRepo.save(p3);

			Permission p4 = new Permission();
			p4.setValue("updateProject");
			p4.setDescription("Project: Update can update project information");
			permissionRepo.save(p4);

			Permission p5 = new Permission();
			p5.setValue("projectAdmin");
			p5.setDescription("PROJECT ADMINISTRATOR: User can perform all actions on projects");
			permissionRepo.save(p5);

			Permission p29 = new Permission();
			p29.setValue("disableProject");
			p29.setDescription("Project: User can disable created projects");
			permissionRepo.save(p29);

			Permission p6 = new Permission();
			p6.setValue("createBundle");
			p6.setDescription("Bundle: User can create project bundles");
			permissionRepo.save(p6);

			Permission p7 = new Permission();
			p7.setValue("deleteBundle");
			p7.setDescription("Bundle: User can delete project bundles");
			permissionRepo.save(p7);

			Permission p8 = new Permission();
			p8.setValue("readBundle");
			p8.setDescription("Bundle: User can view bundle information");
			permissionRepo.save(p8);

			Permission p9 = new Permission();
			p9.setValue("updateBundle");
			p9.setDescription("Bundle: User can update bundle information");
			permissionRepo.save(p9);

			Permission p10 = new Permission();
			p10.setValue("bundleAdmin");
			p10.setDescription("BUNDLE ADMINISTRATOR: User can perform all CRUD actions on bundles");
			permissionRepo.save(p10);

			Permission p11 = new Permission();
			p11.setValue("createInstance");
			p11.setDescription("Instance: User can create platform instances of a project");
			permissionRepo.save(p11);

			Permission p12 = new Permission();
			p12.setValue("readInstance");
			p12.setDescription("Instance: User can read platform instance information except log or console");
			permissionRepo.save(p12);

			Permission p13 = new Permission();
			p13.setValue("deleteInstance");
			p13.setDescription("Instance: User can delete platform instances");
			permissionRepo.save(p13);

			Permission p27 = new Permission();
			p27.setValue("stopInstance");
			p27.setDescription("Instance: User can stop live platform instances");
			permissionRepo.save(p27);

			Permission p28 = new Permission();
			p28.setValue("startInstance");
			p28.setDescription("Instance: User can start dead platform instances");
			permissionRepo.save(p28);

			Permission p30 = new Permission();
			p30.setValue("instanceConsole");
			p30.setDescription("Instance: User can access instance management console");
			permissionRepo.save(p30);

			Permission p40 = new Permission();
			p40.setValue("instanceLog");
			p40.setDescription("Instance: User can access instance logs");
			permissionRepo.save(p40);

			Permission p14 = new Permission();
			p14.setValue("instanceAdmin");
			p14.setDescription("INSTANCE ADMINISTRATOR: User can perform all operations on instances");
			permissionRepo.save(p14);

			Permission p15 = new Permission();
			p15.setValue("signedIn");
			p15.setDescription("Session: User can sign into system");
			permissionRepo.save(p15);

			Permission p16 = new Permission();
			p16.setValue("superAdmin");
			p16.setDescription("Super administrator");
			permissionRepo.save(p16);
			
			Permission p26 = new Permission();
			p26.setValue("admin");
			p26.setDescription("PLATFORM OWNER: Full access to Projects, Instances, Bundles, and Teams");
			permissionRepo.save(p26);

			List<Permission> admin = Arrays.asList(p1, p2, p3, p4, p5, p6, p7,
					p8, p9, p10, p11, p12, p13, p14, p15, p26, p27, p28, p29,
					p30, p40);

			List<Permission> sadmin = Arrays.asList(p1, p2, p3, p4, p5, p6, p7,
					p8, p9, p10, p11, p12, p13, p14, p15, p26, p27, p28, p29,
					p30, p40, p16);

			Role role0 = new Role();
			role0.setName(SUPER_ADMIN_NAME);
			role0.setOwner(web);
			roleRepo.save(role0);

			Role role = new Role();
			role.setName(ADMINISTRATOR_NAME);
			role.setOwner(web);
			roleRepo.save(role);

			Role role2 = new Role();
			role2.setName(AUTHENTICATED_NAME);
			role2.setOwner(web);
			roleRepo.save(role2);

			web.setAssignedRole(role0);
			web.setRole(role0.getName());
			userRepo.save(web);

			// Important to call the role controller to initialize permissions
			RolePermission rp = new RolePermission(web.getFullName(), role0,
					p26);
			rpRepo.save(rp);

			try {
				roleCtrl.addPermission(role.getId(), admin, web.getID());
				roleCtrl.addPermission(role0.getId(), sadmin, web.getID());
				roleCtrl.addPermission(role2.getId(), p15, web.getID());
			} catch (RoleControllerException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}

	}

	@Autowired
	UserController userController;

	@Override
	public Session newSession(Request request, Response response) {
		WebRequest webRequest = (WebRequest) RequestCycle.get().getRequest();
		UserSession session = new UserSession(webRequest);
		Cookie email = webRequest.getCookie(REMEMBER_ME_EMAIL_COOKIE);
		Cookie password = webRequest.getCookie(REMEMBER_ME_PASSWORD_COOKIE);
		if (email != null && password != null) {
			try {
				User user = userController.login(email.getValue(),
						password.getValue());

				if (user != null) {
					session.setUser(user);
				}
				sessions.put(user.getID(), session);
			} catch (AccountLoginException e) {

			}
		}
		return session;
	}
	
	public UserSession getUserSession (Long id) {
		return sessions.get(id);
	}
	
	public Map<Long, UserSession> getSessions () {
		return sessions;
	}

}

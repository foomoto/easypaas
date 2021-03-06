/**
 * 
 */
package com.withinet.opaas.wicket.html;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.withinet.opaas.controller.Authorizer;
import com.withinet.opaas.controller.InstanceController;
import com.withinet.opaas.controller.common.BundleControllerException;
import com.withinet.opaas.controller.common.ControllerSecurityException;
import com.withinet.opaas.controller.common.InstanceControllerException;
import com.withinet.opaas.controller.common.ServiceProperties;
import com.withinet.opaas.model.domain.Instance;
import com.withinet.opaas.model.domain.User;
import com.withinet.opaas.util.EasyReader;
import com.withinet.opaas.wicket.services.UserSession;

/**
 * @author Folarin
 * 
 */
public class InstanceTableWidget extends Panel {
	@SpringBean
	InstanceController instanceController;

	Long selected = null;

	Long pid = null;
	
	private Boolean authorized;
	
	@SpringBean
	Authorizer authorizer;

	private Map.Entry<Integer, StringBuffer> map;
	/**
	 * 
	 */
	private static final long serialVersionUID = 6426148890485840838L;
	private SortableDataProvider<Instance, String> provider = null;
	private final List<IColumn<Instance, String>> columns = Collections
			.synchronizedList(new ArrayList<IColumn<Instance, String>>());
	private int resultSize = 20;

	/**
	 * @param id
	 */
	public InstanceTableWidget(String id) {
		super(id);
	}

	public InstanceTableWidget(String id, Long pid, boolean b) {
		super(id);
		this.pid = pid;
		this.authorized = b;
	}

	public InstanceTableWidget(String id, boolean b) {
		super (id);
		authorized = b;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		setVisible (authorized);
		provider = new InstanceTableDataProvider();
		final Label moreLogLabel = new Label("moreLog");
		moreLogLabel.setOutputMarkupId(true);
		moreLogLabel.setEscapeModelStrings(false);
		add(moreLogLabel);

		IndicatingAjaxLink getMore = new IndicatingAjaxLink("more-log-button") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				target.add(moreLogLabel, moreLogLabel.getMarkupId());
				target.appendJavaScript("addContent()");
			}
		};
		add(getMore);

		columns.add(new PropertyColumn<Instance, String>(new Model<String>(
				"Project Name"), "projectName"));
		columns.add(new PropertyColumn<Instance, String>(new Model<String>(
				"Type"), "containerType"));
		columns.add(new PropertyColumn<Instance, String>(new Model<String>(
				"Status"), "status"));
		columns.add(new PropertyColumn<Instance, String>(new Model<String>(
				"Created by"), "ownerName"));
		columns.add(new PropertyColumn<Instance, String>(new Model<String>(
				"Created"), "created"));
		columns.add(new AbstractColumn<Instance, String>(new Model<String>(
				"Quick Action")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<Instance>> item,
					String componentId, final IModel<Instance> model) {
				// BookmarkablePageLink<InstanceIndex> stopInstance = new
				// BookmarkablePageLink<InstanceIndex> ("stop-instance",
				// InstanceIndex.class, setStopInstanceLinkParameters
				// (model.getObject()));
				IndicatingAjaxLink stopInstance = new IndicatingAjaxLink("stop-instance") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						Long id = model.getObject().getId();
						Long uid = UserSession.get().getUser().getID();
						try {
							instanceController.stopInstance(id, uid);
							getPage().info("Success, instance stopped");
							setResponsePage(getPage());
						} catch (InstanceControllerException e) {
							error(e.getMessage());
						}
					}
				};
				
				IndicatingAjaxLink startInstance = new IndicatingAjaxLink("start-instance") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						Long id = model.getObject().getId();
						Long uid = UserSession.get().getUser().getID();
						try {
							boolean dirty = false;
							instanceController.startInstance(id, uid, dirty);
							getPage().info("Success, instance live");
							setResponsePage(getPage());
						} catch (InstanceControllerException e) {
							error(e.getMessage());
						}
					}
				};
				
				ConfirmationLink<String> flushInstance = new ConfirmationLink<String>("flush-instance", "Instance resources will be deleted permanently?") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						Long id = model.getObject().getId();
						Long uid = UserSession.get().getUser().getID();
						try {
							instanceController.deleteInstance(id, uid);
							getPage().info("Success, instance deleted");
							setResponsePage(getPage());
						} catch (InstanceControllerException e) {
							error(e.getMessage());
						}
					}
				};

				IndicatingAjaxLink log = new IndicatingAjaxLink("view-log") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						Model labelModel = new Model() {
							/**
							 * 
							 */
							private static final long serialVersionUID = 2179484564682841086L;
							private Integer minLogLine = 0;
							private Integer maxLogLine = 20;
							private Integer incrementConstant = 30;

							@Override
							public Serializable getObject() {

								map = EasyReader.getString(model.getObject()
										.getLogFile(), minLogLine, maxLogLine);
								if (map != null) {
									minLogLine = map.getKey();
									maxLogLine = minLogLine + incrementConstant;
									return map.getValue();
								}
								return "";
							}
						};
						moreLogLabel.setDefaultModel(labelModel);
						target.add(moreLogLabel, moreLogLabel.getMarkupId());
						target.appendJavaScript("showModal()");
						target.appendJavaScript("addContent()");
					}
				};

				ExternalLink cpanelLink = new ExternalLink("cpanel-link", model
						.getObject().getCpanelUrl());
				flushInstance.setVisible(false);
				startInstance.setVisible(false);
				stopInstance.setVisible(false);
				cpanelLink.setVisible(false);
				log.setVisible(true);

				if (model.getObject().getStatus().equals("Live")) {
					stopInstance.setVisible(true);
					cpanelLink.setVisible(true);
				}
				if (model.getObject().getStatus().equals("Dead")) {
					flushInstance.setVisible(true);
					startInstance.setVisible(true);
				}
				if (model.getObject().getStatus().equals("Starting")) {
					flushInstance.setVisible(true);
				}

				InstanceTableQuickAction button = new InstanceTableQuickAction(
						componentId, stopInstance, flushInstance,
						startInstance, cpanelLink, log);
				item.add(button);
			}
		});
		DataTable<Instance, String> dataTable = new DefaultDataTable<Instance, String>(
				"instance-table", columns, provider, resultSize);
		add(dataTable);
	}

	private class InstanceTableDataProvider extends
			SortableDataProvider<Instance, String> {

		private List<Instance> userInstances = new ArrayList<Instance>();

		@Override
		public Iterator<? extends Instance> iterator(long arg0, long arg1) {
			return userInstances.subList((int) arg0,
					Math.min((int) userInstances.size(), (int) arg1))
					.iterator();
		}

		@Override
		public IModel<Instance> model(Instance arg0) {
			return Model.of(arg0);
		}

		@Override
		public long size() {
			Long USER_ID = UserSession.get().getUser().getID();
			if (authorized) {
				if (pid != null) {
					try {
						userInstances = instanceController.listInstancesByProject(
								pid, USER_ID);
					} catch (InstanceControllerException e1) {
						error (e1.getMessage ());
						try {
							authorizer.authorize(ServiceProperties.SUPER_ADMIN, USER_ID);
							userInstances = instanceController.listAllInstances(USER_ID);
						} catch (ControllerSecurityException e) {
							try {
								userInstances = instanceController.listInstancesByUser(USER_ID, USER_ID);
							} catch (InstanceControllerException e2) {
								e2.printStackTrace();
								error (e2.getMessage());
							}
						}
						e1.printStackTrace();
					}
				} else {
					try {
						authorizer.authorize(ServiceProperties.SUPER_ADMIN, USER_ID);
						userInstances = instanceController.listAllInstances(USER_ID);
					} catch (ControllerSecurityException e) {
						try {
							userInstances = instanceController.listInstancesByUser(USER_ID, USER_ID);
						} catch (InstanceControllerException e2) {
							e2.printStackTrace();
							error (e2.getMessage());
						}
					}
				}
			}
			else 
				userInstances = Collections.emptyList();
			
			return userInstances.size();
		}

	}

}

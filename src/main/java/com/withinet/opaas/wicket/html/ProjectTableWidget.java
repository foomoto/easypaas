/**
 * 
 */
package com.withinet.opaas.wicket.html;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.withinet.opaas.model.domain.Project;

/**
 * @author Folarin
 *
 */
public class ProjectTableWidget extends Panel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6426148890485840838L;
	private transient SortableDataProvider<Project, String> provider = new ProjectTableDataProvider (1L);
	private final List<IColumn<Project, String>> columns = Collections.synchronizedList(new ArrayList<IColumn<Project, String>>());
	private int resultSize = 20;
	/**
	 * @param id
	 */
	public ProjectTableWidget(String id) {
		super(id);
		
		columns.add(new PropertyColumn<Project, String>(
				new Model<String>("Name"), "name"));
		columns.add(new PropertyColumn<Project, String>(
				new Model<String>("Created"), "created"));
		columns.add(new PropertyColumn<Project, String>(
				new Model<String>("Status"), "status"));
		columns.add(new AbstractColumn<Project, String>(
				new Model<String>("Quick Action")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(Item<ICellPopulator<Project>> item,
					String componentId, IModel<Project> model) {
				// TODO Auto-generated method stub
				BookmarkablePageLink<InstanceIndex> startInstance = new BookmarkablePageLink<InstanceIndex> ("start-instance", InstanceIndex.class, setStartInstanceLinkParameters (model.getObject()));
				BookmarkablePageLink<ProjectIndex> viewProject = new BookmarkablePageLink<ProjectIndex> ("view-project", ProjectIndex.class, setViewProjectLinkParameters (model.getObject()));
				BookmarkablePageLink<ProjectIndex> deleteProject = new BookmarkablePageLink<ProjectIndex> ("delete-project", ProjectIndex.class, setDeleteProjectLinkParameters (model.getObject()));
				BookmarkablePageLink<ProjectIndex> viewBundles = new BookmarkablePageLink<ProjectIndex> ("view-bundles", BundleIndex.class, setBundlesLinkParameters (model.getObject()));
				BookmarkablePageLink<ProjectIndex> viewInstances = new BookmarkablePageLink<ProjectIndex> ("view-instances", InstanceIndex.class, setInstancesLinkParameters (model.getObject()));
				ProjectTableQuickAction button = new ProjectTableQuickAction (componentId, startInstance, viewProject, deleteProject, viewBundles, viewInstances);
				item.add(button);
			}
			private PageParameters setInstancesLinkParameters(Project project) {
				PageParameters linkParameters = new PageParameters();
				linkParameters.add("pid", project.getID());
				return linkParameters;
			}
			private PageParameters setBundlesLinkParameters(Project project) {
				PageParameters linkParameters = new PageParameters();
				linkParameters.add("pid", project.getID());
				return linkParameters;
			}
			private PageParameters setStartInstanceLinkParameters (Project project) {
				PageParameters linkParameters = new PageParameters();
				linkParameters.add("pid", project.getID());
				linkParameters.add("action", "start");
				return linkParameters;
			}
			
			private PageParameters setViewProjectLinkParameters (Project project) {
				PageParameters linkParameters = new PageParameters();
				linkParameters.add("pid", project.getID());
				return linkParameters;
			}
			
			private PageParameters setDeleteProjectLinkParameters(Project project) {
					PageParameters linkParameters = new PageParameters();
					linkParameters.add("pid", project.getID());
					return linkParameters;
			}
		});
		DataTable<Project, String> dataTable = new DefaultDataTable <Project, String> ("project-view-table", columns, provider, resultSize);
		add (dataTable);
		
	}
	
	

}
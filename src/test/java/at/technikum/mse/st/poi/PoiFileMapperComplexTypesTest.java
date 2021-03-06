package at.technikum.mse.st.poi;

import at.technikum.mse.st.CyclicalDependencyException;
import at.technikum.mse.st.Flatten;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class PoiFileMapperComplexTypesTest {

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	@Test
	public void createTemplate_shouldSupportComplexType() throws Exception {
		File file = tmpFolder.newFile("test.xlsx");
		new PoiFileMapper().createTemplate(file, ContainerSingleChild.class);

		assertThat(countColumns(file)).isEqualTo(2);
	}

	@Test
	public void createTemplate_shouldSupportComplexTypeWithSameChildMultipleTimes() throws Exception {
		File file = tmpFolder.newFile("test.xlsx");
		new PoiFileMapper().createTemplate(file, ContainerMultipleChildren.class);

		assertThat(countColumns(file)).isEqualTo(4);
	}

	@Test(expected = CyclicalDependencyException.class)
	public void createTemplate_shouldPreventStackoverflowForCyclicalDependency() throws Exception {
		File file = tmpFolder.newFile("test.xlsx");
		new PoiFileMapper().createTemplate(file, CyclicalContainer.class);
	}

	@Test
	public void read_ShouldSupportComplexTypeWithSameChildMultipleTimes() throws Exception {
		new PoiFileMapper().read(new File(getClass().getClassLoader().getResource("containerMultipleChildren.xlsx")
				.toURI()), ContainerMultipleChildren.class);
	}

	private int countColumns(File file) throws IOException, InvalidFormatException {
		try (Workbook workbook = WorkbookFactory.create(file)) {
			return workbook.getSheetAt(0).getRow(0).getLastCellNum();
		}
	}

	@SuppressWarnings("unused")
	static class ContainerSingleChild {
		private final String name;
		@Flatten
		private final Child child;

		public ContainerSingleChild(String name, Child child) {
			this.name = name;
			this.child = child;
		}
	}

	@SuppressWarnings("unused")
	static class ContainerMultipleChildren {
		private final String name;
		@Flatten
		private final Child child1;
		@Flatten
		private final Child child2;
		@Flatten
		private final AnotherRelation anotherRelation;

		public ContainerMultipleChildren(String name, Child child1, Child child2, AnotherRelation anotherRelation) {
			this.name = name;
			this.child1 = child1;
			this.child2 = child2;
			this.anotherRelation = anotherRelation;
		}
	}

	@SuppressWarnings("unused")
	static class Child {
		private final String name;

		public Child(String name) {
			this.name = name;
		}
	}

	@SuppressWarnings("unused")
	static class AnotherRelation {
		private final int length;

		public AnotherRelation(int length) {
			this.length = length;
		}
	}

	static class CyclicalContainer {
		@Flatten
		private final CyclicalContainer cycle;

		public CyclicalContainer(CyclicalContainer cycle) {
			this.cycle = cycle;
		}
	}
}

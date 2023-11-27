package io.crnk.core.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.crnk.core.exception.MethodNotAllowedException;

public class ReadOnlyRelationshipRepositoryBaseTest {


	private ReadOnlyRelationshipRepositoryBase repo = new ReadOnlyRelationshipRepositoryBase() {

	};

	@Test
	public void getSourceResourceClass() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		repo.getSourceResourceClass();
		});
	}

	@Test
	public void getMatcher() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		repo.getMatcher();
		});
	}

	@Test
	public void getTargetResourceClass() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		repo.getTargetResourceClass();
		});
	}

	@Test
	public void findOneTarget() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
		repo.findOneTarget(null, null, null);
		});
	}

	@Test
	public void findManyTargets() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
		repo.findManyTargets(null, null, null);
		});
	}

	@Test
	public void setRelation() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
		repo.setRelation(null, null, null);
		});
	}

	@Test
	public void setRelations() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
		repo.setRelations(null, null, null);
		});
	}

	@Test
	public void addRelations() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
		repo.addRelations(null, null, null);
		});
	}

	@Test
	public void removeRelations() {
		Assertions.assertThrows(MethodNotAllowedException.class, () -> {
		repo.removeRelations(null, null, null);
		});
	}
}

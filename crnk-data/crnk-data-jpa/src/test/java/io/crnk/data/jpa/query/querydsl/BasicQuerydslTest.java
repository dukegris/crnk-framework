package io.crnk.data.jpa.query.querydsl;

import io.crnk.data.jpa.query.BasicQueryTestBase;
import io.crnk.data.jpa.query.JpaQueryFactory;

import jakarta.persistence.EntityManager;

public class BasicQuerydslTest extends BasicQueryTestBase {

	@Override
	protected JpaQueryFactory createQueryFactory(EntityManager em) {
		return QuerydslQueryFactory.newInstance();
	}
}

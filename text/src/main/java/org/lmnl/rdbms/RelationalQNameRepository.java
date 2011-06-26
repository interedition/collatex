package org.lmnl.rdbms;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.lmnl.QName;
import org.lmnl.QNameRepository;

import com.google.common.collect.Iterables;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;

public class RelationalQNameRepository implements QNameRepository {

	private Map<QNameRelation, Integer> nameCache;
	private int cacheSize = 1000;
	private SessionFactory sessionFactory;

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public QName get(QName name) {
		return Iterables.getOnlyElement(get(Collections.singleton(name)));
	}

	public synchronized Set<QName> get(Set<QName> names) {
		if (nameCache == null) {
			nameCache = new MapMaker().maximumSize(cacheSize).makeMap();
		}

		names = Sets.newHashSet(names);
		final Set<QName> foundNames = Sets.newHashSetWithExpectedSize(names.size());

		final Session session = sessionFactory.getCurrentSession();
		for (Iterator<QName> nameIt = names.iterator(); nameIt.hasNext();) {
			final Integer nameId = nameCache.get(nameIt.next());
			if (nameId != null) {
				final QName found = (QName) session.get(QNameRelation.class, nameId);
				if (found != null) {
					nameIt.remove();
					foundNames.add(found);
				}
			}
		}

		if (!names.isEmpty()) {
			final Disjunction dj = Restrictions.disjunction();
			for (QName name : names) {
				Conjunction cj = Restrictions.conjunction();
				URI namespace = name.getNamespaceURI();
				cj.add(namespace == null ? Restrictions.isNull("namespace") : Restrictions.eq("namespace",
						namespace.toString()));
				cj.add(Restrictions.eq("localName", name.getLocalName()));
				dj.add(cj);
			}

			@SuppressWarnings("unchecked")
			final List<QNameRelation> found = session.createCriteria(QNameRelation.class).add(dj).list();
			for (QNameRelation name : found) {
				foundNames.add(name);
				names.remove(name);
				nameCache.put(name, name.getId());
			}
			
			for (QName name : names) {
				QNameRelation created = new QNameRelation(name.getNamespaceURI(), name.getLocalName());
				session.save(created);
				foundNames.add(created);
				nameCache.put(created, created.getId());
			}
		}

		return foundNames;
	}

	public synchronized void clearCache() {
		nameCache = null;
	}
}

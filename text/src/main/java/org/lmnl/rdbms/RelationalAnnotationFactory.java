package org.lmnl.rdbms;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.hibernate.*;
import org.hibernate.Session;
import org.hibernate.classic.*;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.lmnl.*;

import java.io.IOException;
import java.io.StringReader;

public class RelationalAnnotationFactory {
    public static final Joiner ANCESTOR_JOINER = Joiner.on('.');

    private SessionFactory sessionFactory;

    private QNameRepository nameRepository;

    private TextRepository textRepository;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setNameRepository(QNameRepository nameRepository) {
        this.nameRepository = nameRepository;
    }

    public void setTextRepository(TextRepository textRepository) {
        this.textRepository = textRepository;
    }

    public AnnotationRelation create(Text text, QName name, Range range) {
        Preconditions.checkArgument(text instanceof TextRelation);
        final AnnotationRelation created = new AnnotationRelation();
        created.setText((TextRelation) text);
        created.setName(nameRepository.get(name));
        created.setRange(range == null ? Range.NULL : range);

        sessionFactory.getCurrentSession().save(created);
        return created;
    }

    public void delete(Annotation annotation) {
        Preconditions.checkArgument(annotation instanceof AnnotationRelation);
        final AnnotationRelation relation = (AnnotationRelation) annotation;
        final Session session = sessionFactory.getCurrentSession();
        session.delete(session.get(AnnotationRelation.class, relation.getId()));
    }

    public Text newText() {
        TextRelation textRelation = new TextRelation();
        sessionFactory.getCurrentSession().save(textRelation);
        return textRelation;
    }

    public void delete(Text text) {
        final TextRelation textRelation = (TextRelation) text;
        final Session session = sessionFactory.getCurrentSession();

        final Query deleteAnnotations = session.createQuery("DELETE FROM " + AnnotationRelation.class.getName() + " as a WHERE a.text.id = :textId");
        deleteAnnotations.setInteger("textId", textRelation.getId()).executeUpdate();

        session.delete(session.get(TextRelation.class, textRelation.getId()));
    }
}

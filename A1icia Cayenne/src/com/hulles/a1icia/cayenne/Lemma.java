package com.hulles.a1icia.cayenne;

import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.query.ObjectSelect;

import com.hulles.a1icia.cayenne.auto._Lemma;
import com.hulles.a1icia.tools.A1iciaUtils;

public class Lemma extends _Lemma {
	private final static String DUMMY_LEMMA = "EDIT_ME";
    private static final long serialVersionUID = 1L; 
    
    public static Lemma findLemma(Integer lemmaID) {
		ObjectContext context;
		Lemma lemma;
		
		A1iciaUtils.checkNotNull(lemmaID);
		context = A1iciaApplication.getEntityContext();
		lemma = Cayenne.objectForPK(context, Lemma.class, lemmaID);
		return lemma;
    }

    public static boolean lemmaExists(String word, String pos, String lemma) {
		ObjectContext context;
		Lemma dbLemma = null;
		
		A1iciaUtils.checkNotNull(word);
		A1iciaUtils.checkNotNull(pos);
		A1iciaUtils.checkNotNull(lemma);
		context = A1iciaApplication.getEntityContext();
		dbLemma = ObjectSelect
				.query(Lemma.class)
				.where(_Lemma.WORD.eq(word)
						.andExp(_Lemma.POS.eq(pos))
						.andExp(_Lemma.LEMMA.eq(lemma)))
				.selectOne(context);
		return dbLemma != null;
    }
    
    public static String getDummyLemmaTag() {
    	
    	return DUMMY_LEMMA;
    }
    
	public static List<Lemma> getAllLemmas() {
		ObjectContext context;
		List<Lemma> dbLemmas;
		
		context = A1iciaApplication.getEntityContext();
		dbLemmas = ObjectSelect
				.query(Lemma.class)
				.orderBy("word")
				.orderBy("pos")
				.where(_Lemma.LEMMA.ne(DUMMY_LEMMA))
				.select(context);
		return dbLemmas;
    }
	
    public static List<Lemma> getLemmas(String word, String pos) {
		ObjectContext context;
		List<Lemma> dbLemmas = null;
		
		A1iciaUtils.checkNotNull(word);
		A1iciaUtils.checkNotNull(pos);
		context = A1iciaApplication.getEntityContext();
		dbLemmas = ObjectSelect
				.query(Lemma.class)
				.where(_Lemma.WORD.eq(word)
						.andExp(_Lemma.POS.eq(pos))
						.andExp(_Lemma.LEMMA.ne(DUMMY_LEMMA)))
				.select(context);
		return dbLemmas;
    }
    
    public void commit() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.commitChanges();
    }
    
    public void rollback() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
    	context.rollbackChanges();
    }

	public void delete() {
    	ObjectContext context;
    	
    	context = this.getObjectContext();
     	context.deleteObjects(this);
    	context.commitChanges();
	}

	public static Lemma createNew() {
    	ObjectContext context;
    	Lemma dbLemma;
    	
    	context = A1iciaApplication.getEntityContext();
        dbLemma = context.newObject(Lemma.class);
    	// NOT committed yet
    	return dbLemma;
	}
}

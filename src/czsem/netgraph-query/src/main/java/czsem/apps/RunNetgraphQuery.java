/**
 * Project applications
 */
package czsem.apps;

import java.io.FileOutputStream;

import cz.cuni.mff.mirovsky.account.UserAccount;
import czsem.net.NetgraphProtocolConnection;
import czsem.net.NetgraphServerComunication;
import czsem.net.NetgraphServerComunication.NetgraphConnectionInfo;
import czsem.net.NetgraphServerComunication.TreeSubtypeChars;
import czsem.utils.NetgraphQuery;
import czsem.utils.SimpleXMLQueryProcessor;

/**
 * @author dedek
 *
 */
public class RunNetgraphQuery {
	
	public static void main(String[] args) throws Exception
	{
		NetgraphServerComunication nc = new NetgraphServerComunication();
		NetgraphConnectionInfo ci = nc.openConnection("u-pl33.ms.mff.cuni.cz", 2000);
//		NetgraphConnectionInfo ci = nc.openConnection("localhost", 2000);
		UserAccount ua = nc.login("anonymous", NetgraphProtocolConnection.encryptPassword("anonymous"));
//		UserAccount ua = nc.login("dedek", "50eb3b47fbee4df59eaef6368261063b");
/**/		
		SimpleXMLQueryProcessor sp = new SimpleXMLQueryProcessor(
				new String [] {
						"action_type.t_lemma",
						"a-negation.m/tag",						
						"injury_manner.t_lemma",
						"participant.t_lemma",
						"quantity.t_lemma"},
						new FileOutputStream("query_out.xml"),
						nc);
//		transitive participant
//		String query_string = "[_name=action_type,gram/sempos=v,t_lemma=zranit|usmrtit|zemřít|zahynout|přežít]([m/tag=??????????N*,_name=a-negation,hide=true,_optional=true],[functor=MANN,_name=injury_manner,_optional=true],[functor=ACT|PAT,t_lemma=kdo|člověk|osoba|muž|žena|dítě|řidič|řidička|spolujezdec|spolujezdkyně,_name=participant,_transitive=true]([functor=RSTR,gram/sempos=n.quant.*|adj.quant.*,_name=quantity,_optional=true]))";

		String query_string = "[_name=action_type,gram/sempos=v,t_lemma=zranit|usmrtit|zemřít|zahynout|přežít]([m/tag=??????????N*,_name=a-negation,hide=true,_optional=true],[functor=MANN,_name=injury_manner,_optional=true],[functor=ACT|PAT,t_lemma=kdo|člověk|osoba|muž|žena|dítě|řidič|řidička|spolujezdec|spolujezdkyně,_name=participant,_optional=false]([functor=RSTR,gram/sempos=n.quant.*|adj.quant.*,_name=quantity,_optional=true]))"; 

//		String query_string = "[m/tag=??????????N*,_name=a-negation,_optional=true]"; 

/**
		SimpleXMLQueryProcessor sp = new SimpleXMLQueryProcessor(
				new String [] {
						"amount.t_lemma",
						"unit.t_lemma",
						"material.t_lemma",
						"where.t_lemma"},
						new FileOutputStream("query_out_nafta.xml"),
						nc);

		String query_string = "[t_lemma=uniknout|unikat|vytéci]([_name=unit]([gram/sempos=adj.quant.def,_name=amount],[functor=MAT,_name=material]),[_optional=true,functor=DIR3,_name=where])";

/**/
		
		sp.stratXMLOtput();
		sp.writeNetgraphConnectionInfo(ci);
		sp.writeUserInfo(ua);

		nc.setSearchPathAndInitializeGlobalHead(nc.getCurrentDirectory());
//		String query_string = "[_name=action_type,gram/sempos=v,t_lemma=zranit|usmrtit|zemřít|zahynout|přežít]([m/tag=??????????N*,_name=a-negation,hide=true,_optional=true],[functor=MANN,_name=injury_manner],[functor=ACT|PAT,t_lemma=kdo|člověk|osoba|muž|žena|dítě|řidič|řidička|spolujezdec|spolujezdkyně,_name=participant]([functor=RSTR,gram/sempos=n.quant.*|adj.quant.*,_name=quantity]))";
//		String query_string = "[_name=action_type,gram/sempos=v,t_lemma=zranit|usmrtit|zemřít|zahynout|přežít]([m/tag=??????????N*,_name=a-negation,hide=true,_optional=true],[functor=MANN,_name=injury_manner,_optional=true],[functor=ACT|PAT,t_lemma=kdo|člověk|osoba|muž|žena|dítě|řidič|řidička|spolujezdec|spolujezdkyně,_name=participant,_transitive=true]([functor=RSTR,gram/sempos=n.quant.*|adj.quant.*,_name=quantity,_optional=true]))";
//		String query_string = "[_name=action_type,gram/sempos=v,t_lemma=zranit|usmrtit|zemřít|zahynout|přežít]([m/tag=??????????N*,_name=a-negation,hide=true,_optional=true],[functor=MANN,_name=injury_manner,_optional=true],[functor=ACT|PAT,t_lemma=kdo|člověk|osoba|muž|žena|dítě|řidič|řidička|spolujezdec|spolujezdkyně,_name=participant]([functor=RSTR,gram/sempos=n.quant.*|adj.quant.*,_name=quantity,_optional=true]))"; 
//		String query_string = "AND\n[t_lemma=hasič,_name=action_type]([_name=a-negation])\n[_name=participant]([t_lemma=jihomoravský,_name=injury_manner])";
		
		
/****/
		nc.setTimeOut(30);
/****/
		
		
		NetgraphQuery nq = new NetgraphQuery(query_string, nc);

/****/		
		nq.setResultTreeSubtype(TreeSubtypeChars.GET_TREE_SUBTYPE_TREE);
/****/		
		
		sp.writeQueryInfo(nc.getCurrentDirectory(), nq);
		
//		nq.startQueryAll();
		nq.startTheQuery();
		
		System.err.println(nq.processResult(sp) + " trees");
		
		sp.writeNetgraphQueryStatistics(nc.getStatistics());
		
		sp.closeXMLOtput();
						
		nc.close();
		
		
		//print artrt list
/*		for (int i = 0; i < nc.getGlobalHead().getSize(); i++) {
			System.out.println(i+": "+nc.getGlobalHead().getAttributeAt(i).getName());			
		}
/**/

	}

}

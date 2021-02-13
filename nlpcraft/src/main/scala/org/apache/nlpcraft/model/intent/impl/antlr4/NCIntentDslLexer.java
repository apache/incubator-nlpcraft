// Generated from /Users/nivanov/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4/NCIntentDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.impl.antlr4;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCIntentDslLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, SQSTRING=6, DQSTRING=7, EQ=8, 
		NEQ=9, GTEQ=10, LTEQ=11, GT=12, LT=13, AND=14, OR=15, VERT=16, NOT=17, 
		LPAR=18, RPAR=19, LBRACE=20, RBRACE=21, SQUOTE=22, DQUOTE=23, TILDA=24, 
		LBR=25, RBR=26, POUND=27, COMMA=28, COLON=29, MINUS=30, DOT=31, UNDERSCORE=32, 
		ASSIGN=33, PLUS=34, QUESTION=35, MULT=36, DIV=37, MOD=38, DOLLAR=39, BOOL=40, 
		NULL=41, INT=42, REAL=43, EXP=44, ID=45, WS=46, ErrorCharacter=47;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "SQSTRING", "DQSTRING", "EQ", 
			"NEQ", "GTEQ", "LTEQ", "GT", "LT", "AND", "OR", "VERT", "NOT", "LPAR", 
			"RPAR", "LBRACE", "RBRACE", "SQUOTE", "DQUOTE", "TILDA", "LBR", "RBR", 
			"POUND", "COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", "ASSIGN", "PLUS", 
			"QUESTION", "MULT", "DIV", "MOD", "DOLLAR", "BOOL", "NULL", "INT", "REAL", 
			"EXP", "ID", "WS", "ErrorCharacter"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'ordered'", "'flow'", "'meta'", "'term'", null, null, 
			"'=='", "'!='", "'>='", "'<='", "'>'", "'<'", "'&&'", "'||'", "'|'", 
			"'!'", "'('", "')'", "'{'", "'}'", "'''", "'\"'", "'~'", "'['", "']'", 
			"'#'", "','", "':'", "'-'", "'.'", "'_'", "'='", "'+'", "'?'", "'*'", 
			"'/'", "'%'", "'$'", null, "'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "SQSTRING", "DQSTRING", "EQ", "NEQ", 
			"GTEQ", "LTEQ", "GT", "LT", "AND", "OR", "VERT", "NOT", "LPAR", "RPAR", 
			"LBRACE", "RBRACE", "SQUOTE", "DQUOTE", "TILDA", "LBR", "RBR", "POUND", 
			"COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", "ASSIGN", "PLUS", "QUESTION", 
			"MULT", "DIV", "MOD", "DOLLAR", "BOOL", "NULL", "INT", "REAL", "EXP", 
			"ID", "WS", "ErrorCharacter"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public NCIntentDslLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "NCIntentDsl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\61\u0117\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3"+
		"\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6"+
		"\3\6\3\6\3\6\3\6\3\7\3\7\7\7\u0082\n\7\f\7\16\7\u0085\13\7\3\7\3\7\3\b"+
		"\3\b\7\b\u008b\n\b\f\b\16\b\u008e\13\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\n"+
		"\3\13\3\13\3\13\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3\20\3\20"+
		"\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27"+
		"\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36"+
		"\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3"+
		"(\3(\3)\3)\3)\3)\3)\3)\3)\3)\3)\5)\u00e1\n)\3*\3*\3*\3*\3*\3+\3+\3+\7"+
		"+\u00eb\n+\f+\16+\u00ee\13+\5+\u00f0\n+\3,\3,\6,\u00f4\n,\r,\16,\u00f5"+
		"\3-\3-\5-\u00fa\n-\3-\3-\3.\3.\3.\6.\u0101\n.\r.\16.\u0102\3.\3.\3.\3"+
		".\3.\7.\u010a\n.\f.\16.\u010d\13.\3/\6/\u0110\n/\r/\16/\u0111\3/\3/\3"+
		"\60\3\60\2\2\61\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31"+
		"\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65"+
		"\34\67\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\60_\61\3\2\f\3\2"+
		"))\3\2$$\3\2\63;\4\2\62;aa\3\2\62;\4\2GGgg\4\2--//\4\2C\\c|\5\2\62;C\\"+
		"c|\5\2\13\f\16\17\"\"\2\u0126\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t"+
		"\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2"+
		"\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2"+
		"\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2"+
		"+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2"+
		"\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2"+
		"C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3"+
		"\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2"+
		"\2\2]\3\2\2\2\2_\3\2\2\2\3a\3\2\2\2\5h\3\2\2\2\7p\3\2\2\2\tu\3\2\2\2\13"+
		"z\3\2\2\2\r\177\3\2\2\2\17\u0088\3\2\2\2\21\u0091\3\2\2\2\23\u0094\3\2"+
		"\2\2\25\u0097\3\2\2\2\27\u009a\3\2\2\2\31\u009d\3\2\2\2\33\u009f\3\2\2"+
		"\2\35\u00a1\3\2\2\2\37\u00a4\3\2\2\2!\u00a7\3\2\2\2#\u00a9\3\2\2\2%\u00ab"+
		"\3\2\2\2\'\u00ad\3\2\2\2)\u00af\3\2\2\2+\u00b1\3\2\2\2-\u00b3\3\2\2\2"+
		"/\u00b5\3\2\2\2\61\u00b7\3\2\2\2\63\u00b9\3\2\2\2\65\u00bb\3\2\2\2\67"+
		"\u00bd\3\2\2\29\u00bf\3\2\2\2;\u00c1\3\2\2\2=\u00c3\3\2\2\2?\u00c5\3\2"+
		"\2\2A\u00c7\3\2\2\2C\u00c9\3\2\2\2E\u00cb\3\2\2\2G\u00cd\3\2\2\2I\u00cf"+
		"\3\2\2\2K\u00d1\3\2\2\2M\u00d3\3\2\2\2O\u00d5\3\2\2\2Q\u00e0\3\2\2\2S"+
		"\u00e2\3\2\2\2U\u00ef\3\2\2\2W\u00f1\3\2\2\2Y\u00f7\3\2\2\2[\u0100\3\2"+
		"\2\2]\u010f\3\2\2\2_\u0115\3\2\2\2ab\7k\2\2bc\7p\2\2cd\7v\2\2de\7g\2\2"+
		"ef\7p\2\2fg\7v\2\2g\4\3\2\2\2hi\7q\2\2ij\7t\2\2jk\7f\2\2kl\7g\2\2lm\7"+
		"t\2\2mn\7g\2\2no\7f\2\2o\6\3\2\2\2pq\7h\2\2qr\7n\2\2rs\7q\2\2st\7y\2\2"+
		"t\b\3\2\2\2uv\7o\2\2vw\7g\2\2wx\7v\2\2xy\7c\2\2y\n\3\2\2\2z{\7v\2\2{|"+
		"\7g\2\2|}\7t\2\2}~\7o\2\2~\f\3\2\2\2\177\u0083\5-\27\2\u0080\u0082\n\2"+
		"\2\2\u0081\u0080\3\2\2\2\u0082\u0085\3\2\2\2\u0083\u0081\3\2\2\2\u0083"+
		"\u0084\3\2\2\2\u0084\u0086\3\2\2\2\u0085\u0083\3\2\2\2\u0086\u0087\5-"+
		"\27\2\u0087\16\3\2\2\2\u0088\u008c\5/\30\2\u0089\u008b\n\3\2\2\u008a\u0089"+
		"\3\2\2\2\u008b\u008e\3\2\2\2\u008c\u008a\3\2\2\2\u008c\u008d\3\2\2\2\u008d"+
		"\u008f\3\2\2\2\u008e\u008c\3\2\2\2\u008f\u0090\5/\30\2\u0090\20\3\2\2"+
		"\2\u0091\u0092\7?\2\2\u0092\u0093\7?\2\2\u0093\22\3\2\2\2\u0094\u0095"+
		"\7#\2\2\u0095\u0096\7?\2\2\u0096\24\3\2\2\2\u0097\u0098\7@\2\2\u0098\u0099"+
		"\7?\2\2\u0099\26\3\2\2\2\u009a\u009b\7>\2\2\u009b\u009c\7?\2\2\u009c\30"+
		"\3\2\2\2\u009d\u009e\7@\2\2\u009e\32\3\2\2\2\u009f\u00a0\7>\2\2\u00a0"+
		"\34\3\2\2\2\u00a1\u00a2\7(\2\2\u00a2\u00a3\7(\2\2\u00a3\36\3\2\2\2\u00a4"+
		"\u00a5\7~\2\2\u00a5\u00a6\7~\2\2\u00a6 \3\2\2\2\u00a7\u00a8\7~\2\2\u00a8"+
		"\"\3\2\2\2\u00a9\u00aa\7#\2\2\u00aa$\3\2\2\2\u00ab\u00ac\7*\2\2\u00ac"+
		"&\3\2\2\2\u00ad\u00ae\7+\2\2\u00ae(\3\2\2\2\u00af\u00b0\7}\2\2\u00b0*"+
		"\3\2\2\2\u00b1\u00b2\7\177\2\2\u00b2,\3\2\2\2\u00b3\u00b4\7)\2\2\u00b4"+
		".\3\2\2\2\u00b5\u00b6\7$\2\2\u00b6\60\3\2\2\2\u00b7\u00b8\7\u0080\2\2"+
		"\u00b8\62\3\2\2\2\u00b9\u00ba\7]\2\2\u00ba\64\3\2\2\2\u00bb\u00bc\7_\2"+
		"\2\u00bc\66\3\2\2\2\u00bd\u00be\7%\2\2\u00be8\3\2\2\2\u00bf\u00c0\7.\2"+
		"\2\u00c0:\3\2\2\2\u00c1\u00c2\7<\2\2\u00c2<\3\2\2\2\u00c3\u00c4\7/\2\2"+
		"\u00c4>\3\2\2\2\u00c5\u00c6\7\60\2\2\u00c6@\3\2\2\2\u00c7\u00c8\7a\2\2"+
		"\u00c8B\3\2\2\2\u00c9\u00ca\7?\2\2\u00caD\3\2\2\2\u00cb\u00cc\7-\2\2\u00cc"+
		"F\3\2\2\2\u00cd\u00ce\7A\2\2\u00ceH\3\2\2\2\u00cf\u00d0\7,\2\2\u00d0J"+
		"\3\2\2\2\u00d1\u00d2\7\61\2\2\u00d2L\3\2\2\2\u00d3\u00d4\7\'\2\2\u00d4"+
		"N\3\2\2\2\u00d5\u00d6\7&\2\2\u00d6P\3\2\2\2\u00d7\u00d8\7v\2\2\u00d8\u00d9"+
		"\7t\2\2\u00d9\u00da\7w\2\2\u00da\u00e1\7g\2\2\u00db\u00dc\7h\2\2\u00dc"+
		"\u00dd\7c\2\2\u00dd\u00de\7n\2\2\u00de\u00df\7u\2\2\u00df\u00e1\7g\2\2"+
		"\u00e0\u00d7\3\2\2\2\u00e0\u00db\3\2\2\2\u00e1R\3\2\2\2\u00e2\u00e3\7"+
		"p\2\2\u00e3\u00e4\7w\2\2\u00e4\u00e5\7n\2\2\u00e5\u00e6\7n\2\2\u00e6T"+
		"\3\2\2\2\u00e7\u00f0\7\62\2\2\u00e8\u00ec\t\4\2\2\u00e9\u00eb\t\5\2\2"+
		"\u00ea\u00e9\3\2\2\2\u00eb\u00ee\3\2\2\2\u00ec\u00ea\3\2\2\2\u00ec\u00ed"+
		"\3\2\2\2\u00ed\u00f0\3\2\2\2\u00ee\u00ec\3\2\2\2\u00ef\u00e7\3\2\2\2\u00ef"+
		"\u00e8\3\2\2\2\u00f0V\3\2\2\2\u00f1\u00f3\5? \2\u00f2\u00f4\t\6\2\2\u00f3"+
		"\u00f2\3\2\2\2\u00f4\u00f5\3\2\2\2\u00f5\u00f3\3\2\2\2\u00f5\u00f6\3\2"+
		"\2\2\u00f6X\3\2\2\2\u00f7\u00f9\t\7\2\2\u00f8\u00fa\t\b\2\2\u00f9\u00f8"+
		"\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00fb\3\2\2\2\u00fb\u00fc\5U+\2\u00fc"+
		"Z\3\2\2\2\u00fd\u0101\5A!\2\u00fe\u0101\t\t\2\2\u00ff\u0101\5O(\2\u0100"+
		"\u00fd\3\2\2\2\u0100\u00fe\3\2\2\2\u0100\u00ff\3\2\2\2\u0101\u0102\3\2"+
		"\2\2\u0102\u0100\3\2\2\2\u0102\u0103\3\2\2\2\u0103\u010b\3\2\2\2\u0104"+
		"\u010a\5O(\2\u0105\u010a\t\n\2\2\u0106\u010a\5;\36\2\u0107\u010a\5=\37"+
		"\2\u0108\u010a\5A!\2\u0109\u0104\3\2\2\2\u0109\u0105\3\2\2\2\u0109\u0106"+
		"\3\2\2\2\u0109\u0107\3\2\2\2\u0109\u0108\3\2\2\2\u010a\u010d\3\2\2\2\u010b"+
		"\u0109\3\2\2\2\u010b\u010c\3\2\2\2\u010c\\\3\2\2\2\u010d\u010b\3\2\2\2"+
		"\u010e\u0110\t\13\2\2\u010f\u010e\3\2\2\2\u0110\u0111\3\2\2\2\u0111\u010f"+
		"\3\2\2\2\u0111\u0112\3\2\2\2\u0112\u0113\3\2\2\2\u0113\u0114\b/\2\2\u0114"+
		"^\3\2\2\2\u0115\u0116\13\2\2\2\u0116`\3\2\2\2\17\2\u0083\u008c\u00e0\u00ec"+
		"\u00ef\u00f5\u00f9\u0100\u0102\u0109\u010b\u0111\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
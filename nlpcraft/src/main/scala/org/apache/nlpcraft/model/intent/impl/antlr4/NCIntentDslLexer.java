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
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, SQSTRING=6, DQSTRING=7, PRED_OP=8, 
		AND=9, OR=10, VERT=11, EXCL=12, LPAREN=13, RPAREN=14, LCURLY=15, RCURLY=16, 
		SQUOTE=17, DQUOTE=18, TILDA=19, RIGHT=20, LBR=21, RBR=22, POUND=23, COMMA=24, 
		COLON=25, MINUS=26, DOT=27, UNDERSCORE=28, EQ=29, PLUS=30, QUESTION=31, 
		STAR=32, FSLASH=33, PERCENT=34, DOLLAR=35, POWER=36, BOOL=37, NULL=38, 
		INT=39, REAL=40, EXP=41, ID=42, WS=43, ErrorCharacter=44;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "SQSTRING", "DQSTRING", "PRED_OP", 
			"AND", "OR", "VERT", "EXCL", "LPAREN", "RPAREN", "LCURLY", "RCURLY", 
			"SQUOTE", "DQUOTE", "TILDA", "RIGHT", "LBR", "RBR", "POUND", "COMMA", 
			"COLON", "MINUS", "DOT", "UNDERSCORE", "EQ", "PLUS", "QUESTION", "STAR", 
			"FSLASH", "PERCENT", "DOLLAR", "POWER", "BOOL", "NULL", "INT", "REAL", 
			"EXP", "ID", "WS", "ErrorCharacter"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'ordered'", "'flow'", "'meta'", "'term'", null, null, 
			null, "'&&'", "'||'", "'|'", "'!'", "'('", "')'", "'{'", "'}'", "'''", 
			"'\"'", "'~'", "'>>'", "'['", "']'", "'#'", "','", "':'", "'-'", "'.'", 
			"'_'", "'='", "'+'", "'?'", "'*'", "'/'", "'%'", "'$'", "'^'", null, 
			"'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "SQSTRING", "DQSTRING", "PRED_OP", 
			"AND", "OR", "VERT", "EXCL", "LPAREN", "RPAREN", "LCURLY", "RCURLY", 
			"SQUOTE", "DQUOTE", "TILDA", "RIGHT", "LBR", "RBR", "POUND", "COMMA", 
			"COLON", "MINUS", "DOT", "UNDERSCORE", "EQ", "PLUS", "QUESTION", "STAR", 
			"FSLASH", "PERCENT", "DOLLAR", "POWER", "BOOL", "NULL", "INT", "REAL", 
			"EXP", "ID", "WS", "ErrorCharacter"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2.\u0115\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\7"+
		"\7|\n\7\f\7\16\7\177\13\7\3\7\3\7\3\b\3\b\7\b\u0085\n\b\f\b\16\b\u0088"+
		"\13\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t"+
		"\u0099\n\t\3\n\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3"+
		"\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\25\3"+
		"\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3"+
		"\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&"+
		"\3&\3&\3&\3&\3&\3&\3&\3&\5&\u00df\n&\3\'\3\'\3\'\3\'\3\'\3(\3(\3(\7(\u00e9"+
		"\n(\f(\16(\u00ec\13(\5(\u00ee\n(\3)\3)\6)\u00f2\n)\r)\16)\u00f3\3*\3*"+
		"\5*\u00f8\n*\3*\3*\3+\3+\3+\6+\u00ff\n+\r+\16+\u0100\3+\3+\3+\3+\3+\7"+
		"+\u0108\n+\f+\16+\u010b\13+\3,\6,\u010e\n,\r,\16,\u010f\3,\3,\3-\3-\2"+
		"\2.\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36"+
		";\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.\3\2\r\3\2))\3\2$$\4\2>>@@\3\2\63"+
		";\4\2\62;aa\3\2\62;\4\2GGgg\4\2--//\4\2C\\c|\5\2\62;C\\c|\5\2\13\f\16"+
		"\17\"\"\2\u012a\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3"+
		"\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2"+
		"\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3"+
		"\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2"+
		"\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\2"+
		"9\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3"+
		"\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2"+
		"\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\3[\3\2\2\2\5b\3\2\2\2\7"+
		"j\3\2\2\2\to\3\2\2\2\13t\3\2\2\2\ry\3\2\2\2\17\u0082\3\2\2\2\21\u0098"+
		"\3\2\2\2\23\u009a\3\2\2\2\25\u009d\3\2\2\2\27\u00a0\3\2\2\2\31\u00a2\3"+
		"\2\2\2\33\u00a4\3\2\2\2\35\u00a6\3\2\2\2\37\u00a8\3\2\2\2!\u00aa\3\2\2"+
		"\2#\u00ac\3\2\2\2%\u00ae\3\2\2\2\'\u00b0\3\2\2\2)\u00b2\3\2\2\2+\u00b5"+
		"\3\2\2\2-\u00b7\3\2\2\2/\u00b9\3\2\2\2\61\u00bb\3\2\2\2\63\u00bd\3\2\2"+
		"\2\65\u00bf\3\2\2\2\67\u00c1\3\2\2\29\u00c3\3\2\2\2;\u00c5\3\2\2\2=\u00c7"+
		"\3\2\2\2?\u00c9\3\2\2\2A\u00cb\3\2\2\2C\u00cd\3\2\2\2E\u00cf\3\2\2\2G"+
		"\u00d1\3\2\2\2I\u00d3\3\2\2\2K\u00de\3\2\2\2M\u00e0\3\2\2\2O\u00ed\3\2"+
		"\2\2Q\u00ef\3\2\2\2S\u00f5\3\2\2\2U\u00fe\3\2\2\2W\u010d\3\2\2\2Y\u0113"+
		"\3\2\2\2[\\\7k\2\2\\]\7p\2\2]^\7v\2\2^_\7g\2\2_`\7p\2\2`a\7v\2\2a\4\3"+
		"\2\2\2bc\7q\2\2cd\7t\2\2de\7f\2\2ef\7g\2\2fg\7t\2\2gh\7g\2\2hi\7f\2\2"+
		"i\6\3\2\2\2jk\7h\2\2kl\7n\2\2lm\7q\2\2mn\7y\2\2n\b\3\2\2\2op\7o\2\2pq"+
		"\7g\2\2qr\7v\2\2rs\7c\2\2s\n\3\2\2\2tu\7v\2\2uv\7g\2\2vw\7t\2\2wx\7o\2"+
		"\2x\f\3\2\2\2y}\5#\22\2z|\n\2\2\2{z\3\2\2\2|\177\3\2\2\2}{\3\2\2\2}~\3"+
		"\2\2\2~\u0080\3\2\2\2\177}\3\2\2\2\u0080\u0081\5#\22\2\u0081\16\3\2\2"+
		"\2\u0082\u0086\5%\23\2\u0083\u0085\n\3\2\2\u0084\u0083\3\2\2\2\u0085\u0088"+
		"\3\2\2\2\u0086\u0084\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0089\3\2\2\2\u0088"+
		"\u0086\3\2\2\2\u0089\u008a\5%\23\2\u008a\20\3\2\2\2\u008b\u008c\7?\2\2"+
		"\u008c\u0099\7?\2\2\u008d\u008e\7#\2\2\u008e\u0099\7?\2\2\u008f\u0090"+
		"\7@\2\2\u0090\u0099\7?\2\2\u0091\u0092\7>\2\2\u0092\u0099\7?\2\2\u0093"+
		"\u0099\t\4\2\2\u0094\u0095\7B\2\2\u0095\u0099\7B\2\2\u0096\u0097\7#\2"+
		"\2\u0097\u0099\7B\2\2\u0098\u008b\3\2\2\2\u0098\u008d\3\2\2\2\u0098\u008f"+
		"\3\2\2\2\u0098\u0091\3\2\2\2\u0098\u0093\3\2\2\2\u0098\u0094\3\2\2\2\u0098"+
		"\u0096\3\2\2\2\u0099\22\3\2\2\2\u009a\u009b\7(\2\2\u009b\u009c\7(\2\2"+
		"\u009c\24\3\2\2\2\u009d\u009e\7~\2\2\u009e\u009f\7~\2\2\u009f\26\3\2\2"+
		"\2\u00a0\u00a1\7~\2\2\u00a1\30\3\2\2\2\u00a2\u00a3\7#\2\2\u00a3\32\3\2"+
		"\2\2\u00a4\u00a5\7*\2\2\u00a5\34\3\2\2\2\u00a6\u00a7\7+\2\2\u00a7\36\3"+
		"\2\2\2\u00a8\u00a9\7}\2\2\u00a9 \3\2\2\2\u00aa\u00ab\7\177\2\2\u00ab\""+
		"\3\2\2\2\u00ac\u00ad\7)\2\2\u00ad$\3\2\2\2\u00ae\u00af\7$\2\2\u00af&\3"+
		"\2\2\2\u00b0\u00b1\7\u0080\2\2\u00b1(\3\2\2\2\u00b2\u00b3\7@\2\2\u00b3"+
		"\u00b4\7@\2\2\u00b4*\3\2\2\2\u00b5\u00b6\7]\2\2\u00b6,\3\2\2\2\u00b7\u00b8"+
		"\7_\2\2\u00b8.\3\2\2\2\u00b9\u00ba\7%\2\2\u00ba\60\3\2\2\2\u00bb\u00bc"+
		"\7.\2\2\u00bc\62\3\2\2\2\u00bd\u00be\7<\2\2\u00be\64\3\2\2\2\u00bf\u00c0"+
		"\7/\2\2\u00c0\66\3\2\2\2\u00c1\u00c2\7\60\2\2\u00c28\3\2\2\2\u00c3\u00c4"+
		"\7a\2\2\u00c4:\3\2\2\2\u00c5\u00c6\7?\2\2\u00c6<\3\2\2\2\u00c7\u00c8\7"+
		"-\2\2\u00c8>\3\2\2\2\u00c9\u00ca\7A\2\2\u00ca@\3\2\2\2\u00cb\u00cc\7,"+
		"\2\2\u00ccB\3\2\2\2\u00cd\u00ce\7\61\2\2\u00ceD\3\2\2\2\u00cf\u00d0\7"+
		"\'\2\2\u00d0F\3\2\2\2\u00d1\u00d2\7&\2\2\u00d2H\3\2\2\2\u00d3\u00d4\7"+
		"`\2\2\u00d4J\3\2\2\2\u00d5\u00d6\7v\2\2\u00d6\u00d7\7t\2\2\u00d7\u00d8"+
		"\7w\2\2\u00d8\u00df\7g\2\2\u00d9\u00da\7h\2\2\u00da\u00db\7c\2\2\u00db"+
		"\u00dc\7n\2\2\u00dc\u00dd\7u\2\2\u00dd\u00df\7g\2\2\u00de\u00d5\3\2\2"+
		"\2\u00de\u00d9\3\2\2\2\u00dfL\3\2\2\2\u00e0\u00e1\7p\2\2\u00e1\u00e2\7"+
		"w\2\2\u00e2\u00e3\7n\2\2\u00e3\u00e4\7n\2\2\u00e4N\3\2\2\2\u00e5\u00ee"+
		"\7\62\2\2\u00e6\u00ea\t\5\2\2\u00e7\u00e9\t\6\2\2\u00e8\u00e7\3\2\2\2"+
		"\u00e9\u00ec\3\2\2\2\u00ea\u00e8\3\2\2\2\u00ea\u00eb\3\2\2\2\u00eb\u00ee"+
		"\3\2\2\2\u00ec\u00ea\3\2\2\2\u00ed\u00e5\3\2\2\2\u00ed\u00e6\3\2\2\2\u00ee"+
		"P\3\2\2\2\u00ef\u00f1\5\67\34\2\u00f0\u00f2\t\7\2\2\u00f1\u00f0\3\2\2"+
		"\2\u00f2\u00f3\3\2\2\2\u00f3\u00f1\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4R"+
		"\3\2\2\2\u00f5\u00f7\t\b\2\2\u00f6\u00f8\t\t\2\2\u00f7\u00f6\3\2\2\2\u00f7"+
		"\u00f8\3\2\2\2\u00f8\u00f9\3\2\2\2\u00f9\u00fa\5O(\2\u00faT\3\2\2\2\u00fb"+
		"\u00ff\59\35\2\u00fc\u00ff\t\n\2\2\u00fd\u00ff\5G$\2\u00fe\u00fb\3\2\2"+
		"\2\u00fe\u00fc\3\2\2\2\u00fe\u00fd\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u00fe"+
		"\3\2\2\2\u0100\u0101\3\2\2\2\u0101\u0109\3\2\2\2\u0102\u0108\5G$\2\u0103"+
		"\u0108\t\13\2\2\u0104\u0108\5\63\32\2\u0105\u0108\5\65\33\2\u0106\u0108"+
		"\59\35\2\u0107\u0102\3\2\2\2\u0107\u0103\3\2\2\2\u0107\u0104\3\2\2\2\u0107"+
		"\u0105\3\2\2\2\u0107\u0106\3\2\2\2\u0108\u010b\3\2\2\2\u0109\u0107\3\2"+
		"\2\2\u0109\u010a\3\2\2\2\u010aV\3\2\2\2\u010b\u0109\3\2\2\2\u010c\u010e"+
		"\t\f\2\2\u010d\u010c\3\2\2\2\u010e\u010f\3\2\2\2\u010f\u010d\3\2\2\2\u010f"+
		"\u0110\3\2\2\2\u0110\u0111\3\2\2\2\u0111\u0112\b,\2\2\u0112X\3\2\2\2\u0113"+
		"\u0114\13\2\2\2\u0114Z\3\2\2\2\20\2}\u0086\u0098\u00de\u00ea\u00ed\u00f3"+
		"\u00f7\u00fe\u0100\u0107\u0109\u010f\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
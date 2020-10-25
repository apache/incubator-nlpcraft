// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4\NCIntentDsl.g4 by ANTLR 4.8
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
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, PRED_OP=14, AND=15, OR=16, VERT=17, 
		EXCL=18, LPAREN=19, RPAREN=20, LCURLY=21, RCURLY=22, SQUOTE=23, TILDA=24, 
		RIGHT=25, LBR=26, RBR=27, COMMA=28, COLON=29, MINUS=30, DOT=31, UNDERSCORE=32, 
		EQ=33, PLUS=34, QUESTION=35, STAR=36, BOOL=37, INT=38, EXP=39, ID=40, 
		WS=41, ErrorCharacter=42;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "PRED_OP", "AND", "OR", "VERT", "EXCL", 
			"LPAREN", "RPAREN", "LCURLY", "RCURLY", "SQUOTE", "TILDA", "RIGHT", "LBR", 
			"RBR", "COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", "EQ", "PLUS", 
			"QUESTION", "STAR", "BOOL", "INT", "EXP", "ID", "WS", "ErrorCharacter"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'ordered'", "'flow'", "'term'", "'id'", "'aliases'", 
			"'startidx'", "'endidx'", "'parent'", "'groups'", "'ancestors'", "'value'", 
			"'null'", null, "'&&'", "'||'", "'|'", "'!'", "'('", "')'", "'{'", "'}'", 
			"'''", "'~'", "'>>'", "'['", "']'", "','", "':'", "'-'", "'.'", "'_'", 
			"'='", "'+'", "'?'", "'*'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, "PRED_OP", "AND", "OR", "VERT", "EXCL", "LPAREN", "RPAREN", 
			"LCURLY", "RCURLY", "SQUOTE", "TILDA", "RIGHT", "LBR", "RBR", "COMMA", 
			"COLON", "MINUS", "DOT", "UNDERSCORE", "EQ", "PLUS", "QUESTION", "STAR", 
			"BOOL", "INT", "EXP", "ID", "WS", "ErrorCharacter"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2,\u011f\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\3"+
		"\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4"+
		"\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\f\3\f\3"+
		"\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16"+
		"\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\5\17\u00bc\n\17\3\20\3\20\3\20\3\21\3\21\3\21\3\22\3\22\3\23\3\23"+
		"\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32"+
		"\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3"+
		"\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3&\3&\3&\3&\3&\3&\3&\5&\u00f6\n&\3\'\3"+
		"\'\3\'\7\'\u00fb\n\'\f\'\16\'\u00fe\13\'\5\'\u0100\n\'\3(\3(\6(\u0104"+
		"\n(\r(\16(\u0105\3)\3)\6)\u010a\n)\r)\16)\u010b\3)\3)\3)\3)\7)\u0112\n"+
		")\f)\16)\u0115\13)\3*\6*\u0118\n*\r*\16*\u0119\3*\3*\3+\3+\2\2,\3\3\5"+
		"\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21"+
		"!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!"+
		"A\"C#E$G%I&K\'M(O)Q*S+U,\3\2\t\4\2>>@@\3\2\63;\4\2\62;aa\3\2\62;\4\2C"+
		"\\c|\5\2\62;C\\c|\5\2\13\f\16\17\"\"\2\u012f\2\3\3\2\2\2\2\5\3\2\2\2\2"+
		"\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2"+
		"\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2"+
		"\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2"+
		"\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2"+
		"\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2"+
		"\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2"+
		"M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\3W\3\2\2\2\5^\3"+
		"\2\2\2\7f\3\2\2\2\tk\3\2\2\2\13p\3\2\2\2\rs\3\2\2\2\17{\3\2\2\2\21\u0084"+
		"\3\2\2\2\23\u008b\3\2\2\2\25\u0092\3\2\2\2\27\u0099\3\2\2\2\31\u00a3\3"+
		"\2\2\2\33\u00a9\3\2\2\2\35\u00bb\3\2\2\2\37\u00bd\3\2\2\2!\u00c0\3\2\2"+
		"\2#\u00c3\3\2\2\2%\u00c5\3\2\2\2\'\u00c7\3\2\2\2)\u00c9\3\2\2\2+\u00cb"+
		"\3\2\2\2-\u00cd\3\2\2\2/\u00cf\3\2\2\2\61\u00d1\3\2\2\2\63\u00d3\3\2\2"+
		"\2\65\u00d6\3\2\2\2\67\u00d8\3\2\2\29\u00da\3\2\2\2;\u00dc\3\2\2\2=\u00de"+
		"\3\2\2\2?\u00e0\3\2\2\2A\u00e2\3\2\2\2C\u00e4\3\2\2\2E\u00e6\3\2\2\2G"+
		"\u00e8\3\2\2\2I\u00ea\3\2\2\2K\u00f5\3\2\2\2M\u00ff\3\2\2\2O\u0101\3\2"+
		"\2\2Q\u0109\3\2\2\2S\u0117\3\2\2\2U\u011d\3\2\2\2WX\7k\2\2XY\7p\2\2YZ"+
		"\7v\2\2Z[\7g\2\2[\\\7p\2\2\\]\7v\2\2]\4\3\2\2\2^_\7q\2\2_`\7t\2\2`a\7"+
		"f\2\2ab\7g\2\2bc\7t\2\2cd\7g\2\2de\7f\2\2e\6\3\2\2\2fg\7h\2\2gh\7n\2\2"+
		"hi\7q\2\2ij\7y\2\2j\b\3\2\2\2kl\7v\2\2lm\7g\2\2mn\7t\2\2no\7o\2\2o\n\3"+
		"\2\2\2pq\7k\2\2qr\7f\2\2r\f\3\2\2\2st\7c\2\2tu\7n\2\2uv\7k\2\2vw\7c\2"+
		"\2wx\7u\2\2xy\7g\2\2yz\7u\2\2z\16\3\2\2\2{|\7u\2\2|}\7v\2\2}~\7c\2\2~"+
		"\177\7t\2\2\177\u0080\7v\2\2\u0080\u0081\7k\2\2\u0081\u0082\7f\2\2\u0082"+
		"\u0083\7z\2\2\u0083\20\3\2\2\2\u0084\u0085\7g\2\2\u0085\u0086\7p\2\2\u0086"+
		"\u0087\7f\2\2\u0087\u0088\7k\2\2\u0088\u0089\7f\2\2\u0089\u008a\7z\2\2"+
		"\u008a\22\3\2\2\2\u008b\u008c\7r\2\2\u008c\u008d\7c\2\2\u008d\u008e\7"+
		"t\2\2\u008e\u008f\7g\2\2\u008f\u0090\7p\2\2\u0090\u0091\7v\2\2\u0091\24"+
		"\3\2\2\2\u0092\u0093\7i\2\2\u0093\u0094\7t\2\2\u0094\u0095\7q\2\2\u0095"+
		"\u0096\7w\2\2\u0096\u0097\7r\2\2\u0097\u0098\7u\2\2\u0098\26\3\2\2\2\u0099"+
		"\u009a\7c\2\2\u009a\u009b\7p\2\2\u009b\u009c\7e\2\2\u009c\u009d\7g\2\2"+
		"\u009d\u009e\7u\2\2\u009e\u009f\7v\2\2\u009f\u00a0\7q\2\2\u00a0\u00a1"+
		"\7t\2\2\u00a1\u00a2\7u\2\2\u00a2\30\3\2\2\2\u00a3\u00a4\7x\2\2\u00a4\u00a5"+
		"\7c\2\2\u00a5\u00a6\7n\2\2\u00a6\u00a7\7w\2\2\u00a7\u00a8\7g\2\2\u00a8"+
		"\32\3\2\2\2\u00a9\u00aa\7p\2\2\u00aa\u00ab\7w\2\2\u00ab\u00ac\7n\2\2\u00ac"+
		"\u00ad\7n\2\2\u00ad\34\3\2\2\2\u00ae\u00af\7?\2\2\u00af\u00bc\7?\2\2\u00b0"+
		"\u00b1\7#\2\2\u00b1\u00bc\7?\2\2\u00b2\u00b3\7@\2\2\u00b3\u00bc\7?\2\2"+
		"\u00b4\u00b5\7>\2\2\u00b5\u00bc\7?\2\2\u00b6\u00bc\t\2\2\2\u00b7\u00b8"+
		"\7B\2\2\u00b8\u00bc\7B\2\2\u00b9\u00ba\7#\2\2\u00ba\u00bc\7B\2\2\u00bb"+
		"\u00ae\3\2\2\2\u00bb\u00b0\3\2\2\2\u00bb\u00b2\3\2\2\2\u00bb\u00b4\3\2"+
		"\2\2\u00bb\u00b6\3\2\2\2\u00bb\u00b7\3\2\2\2\u00bb\u00b9\3\2\2\2\u00bc"+
		"\36\3\2\2\2\u00bd\u00be\7(\2\2\u00be\u00bf\7(\2\2\u00bf \3\2\2\2\u00c0"+
		"\u00c1\7~\2\2\u00c1\u00c2\7~\2\2\u00c2\"\3\2\2\2\u00c3\u00c4\7~\2\2\u00c4"+
		"$\3\2\2\2\u00c5\u00c6\7#\2\2\u00c6&\3\2\2\2\u00c7\u00c8\7*\2\2\u00c8("+
		"\3\2\2\2\u00c9\u00ca\7+\2\2\u00ca*\3\2\2\2\u00cb\u00cc\7}\2\2\u00cc,\3"+
		"\2\2\2\u00cd\u00ce\7\177\2\2\u00ce.\3\2\2\2\u00cf\u00d0\7)\2\2\u00d0\60"+
		"\3\2\2\2\u00d1\u00d2\7\u0080\2\2\u00d2\62\3\2\2\2\u00d3\u00d4\7@\2\2\u00d4"+
		"\u00d5\7@\2\2\u00d5\64\3\2\2\2\u00d6\u00d7\7]\2\2\u00d7\66\3\2\2\2\u00d8"+
		"\u00d9\7_\2\2\u00d98\3\2\2\2\u00da\u00db\7.\2\2\u00db:\3\2\2\2\u00dc\u00dd"+
		"\7<\2\2\u00dd<\3\2\2\2\u00de\u00df\7/\2\2\u00df>\3\2\2\2\u00e0\u00e1\7"+
		"\60\2\2\u00e1@\3\2\2\2\u00e2\u00e3\7a\2\2\u00e3B\3\2\2\2\u00e4\u00e5\7"+
		"?\2\2\u00e5D\3\2\2\2\u00e6\u00e7\7-\2\2\u00e7F\3\2\2\2\u00e8\u00e9\7A"+
		"\2\2\u00e9H\3\2\2\2\u00ea\u00eb\7,\2\2\u00ebJ\3\2\2\2\u00ec\u00ed\7v\2"+
		"\2\u00ed\u00ee\7t\2\2\u00ee\u00ef\7w\2\2\u00ef\u00f6\7g\2\2\u00f0\u00f1"+
		"\7h\2\2\u00f1\u00f2\7c\2\2\u00f2\u00f3\7n\2\2\u00f3\u00f4\7u\2\2\u00f4"+
		"\u00f6\7g\2\2\u00f5\u00ec\3\2\2\2\u00f5\u00f0\3\2\2\2\u00f6L\3\2\2\2\u00f7"+
		"\u0100\7\62\2\2\u00f8\u00fc\t\3\2\2\u00f9\u00fb\t\4\2\2\u00fa\u00f9\3"+
		"\2\2\2\u00fb\u00fe\3\2\2\2\u00fc\u00fa\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fd"+
		"\u0100\3\2\2\2\u00fe\u00fc\3\2\2\2\u00ff\u00f7\3\2\2\2\u00ff\u00f8\3\2"+
		"\2\2\u0100N\3\2\2\2\u0101\u0103\5? \2\u0102\u0104\t\5\2\2\u0103\u0102"+
		"\3\2\2\2\u0104\u0105\3\2\2\2\u0105\u0103\3\2\2\2\u0105\u0106\3\2\2\2\u0106"+
		"P\3\2\2\2\u0107\u010a\5A!\2\u0108\u010a\t\6\2\2\u0109\u0107\3\2\2\2\u0109"+
		"\u0108\3\2\2\2\u010a\u010b\3\2\2\2\u010b\u0109\3\2\2\2\u010b\u010c\3\2"+
		"\2\2\u010c\u0113\3\2\2\2\u010d\u0112\t\7\2\2\u010e\u0112\5;\36\2\u010f"+
		"\u0112\5=\37\2\u0110\u0112\5A!\2\u0111\u010d\3\2\2\2\u0111\u010e\3\2\2"+
		"\2\u0111\u010f\3\2\2\2\u0111\u0110\3\2\2\2\u0112\u0115\3\2\2\2\u0113\u0111"+
		"\3\2\2\2\u0113\u0114\3\2\2\2\u0114R\3\2\2\2\u0115\u0113\3\2\2\2\u0116"+
		"\u0118\t\b\2\2\u0117\u0116\3\2\2\2\u0118\u0119\3\2\2\2\u0119\u0117\3\2"+
		"\2\2\u0119\u011a\3\2\2\2\u011a\u011b\3\2\2\2\u011b\u011c\b*\2\2\u011c"+
		"T\3\2\2\2\u011d\u011e\13\2\2\2\u011eV\3\2\2\2\r\2\u00bb\u00f5\u00fc\u00ff"+
		"\u0105\u0109\u010b\u0111\u0113\u0119\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/probe/mgrs/model/antlr4\NCSynonymDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.probe.mgrs.model.antlr4;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCSynonymDslLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		SQSTRING=10, DQSTRING=11, PRED_OP=12, AND=13, OR=14, EXCL=15, LPAREN=16, 
		RPAREN=17, SQUOTE=18, DQUOTE=19, DOLLAR=20, TILDA=21, LBR=22, RBR=23, 
		COMMA=24, COLON=25, POUND=26, MINUS=27, DOT=28, UNDERSCORE=29, BOOL=30, 
		INT=31, EXP=32, ID=33, WS=34, ErrorCharacter=35;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"SQSTRING", "DQSTRING", "PRED_OP", "AND", "OR", "EXCL", "LPAREN", "RPAREN", 
			"SQUOTE", "DQUOTE", "DOLLAR", "TILDA", "LBR", "RBR", "COMMA", "COLON", 
			"POUND", "MINUS", "DOT", "UNDERSCORE", "BOOL", "INT", "EXP", "ID", "WS", 
			"ErrorCharacter"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'null'", "'id'", "'aliases'", "'startidx'", "'endidx'", "'parent'", 
			"'groups'", "'ancestors'", "'value'", null, null, null, "'&&'", "'||'", 
			"'!'", "'('", "')'", "'''", "'\"'", "'$'", "'~'", "'['", "']'", "','", 
			"':'", "'#'", "'-'", "'.'", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, "SQSTRING", 
			"DQSTRING", "PRED_OP", "AND", "OR", "EXCL", "LPAREN", "RPAREN", "SQUOTE", 
			"DQUOTE", "DOLLAR", "TILDA", "LBR", "RBR", "COMMA", "COLON", "POUND", 
			"MINUS", "DOT", "UNDERSCORE", "BOOL", "INT", "EXP", "ID", "WS", "ErrorCharacter"
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


	public NCSynonymDslLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "NCSynonymDsl.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2%\u00ff\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3"+
		"\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3"+
		"\13\7\13\u008a\n\13\f\13\16\13\u008d\13\13\3\13\3\13\3\f\3\f\7\f\u0093"+
		"\n\f\f\f\16\f\u0096\13\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\3\r\5\r\u00a7\n\r\3\16\3\16\3\16\3\17\3\17\3\17\3\20\3\20\3"+
		"\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3"+
		"\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3"+
		"\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\5\37\u00d6\n\37\3 \3 \3 \7"+
		" \u00db\n \f \16 \u00de\13 \5 \u00e0\n \3!\3!\6!\u00e4\n!\r!\16!\u00e5"+
		"\3\"\3\"\6\"\u00ea\n\"\r\"\16\"\u00eb\3\"\3\"\3\"\3\"\7\"\u00f2\n\"\f"+
		"\"\16\"\u00f5\13\"\3#\6#\u00f8\n#\r#\16#\u00f9\3#\3#\3$\3$\2\2%\3\3\5"+
		"\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21"+
		"!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!"+
		"A\"C#E$G%\3\2\13\3\2))\3\2$$\4\2>>@@\3\2\63;\4\2\62;aa\3\2\62;\4\2C\\"+
		"c|\5\2\62;C\\c|\5\2\13\f\16\17\"\"\2\u0111\2\3\3\2\2\2\2\5\3\2\2\2\2\7"+
		"\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2"+
		"\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2"+
		"\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2"+
		"\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2"+
		"\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2"+
		"\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\3I\3\2\2\2\5N\3\2\2\2\7Q"+
		"\3\2\2\2\tY\3\2\2\2\13b\3\2\2\2\ri\3\2\2\2\17p\3\2\2\2\21w\3\2\2\2\23"+
		"\u0081\3\2\2\2\25\u0087\3\2\2\2\27\u0090\3\2\2\2\31\u00a6\3\2\2\2\33\u00a8"+
		"\3\2\2\2\35\u00ab\3\2\2\2\37\u00ae\3\2\2\2!\u00b0\3\2\2\2#\u00b2\3\2\2"+
		"\2%\u00b4\3\2\2\2\'\u00b6\3\2\2\2)\u00b8\3\2\2\2+\u00ba\3\2\2\2-\u00bc"+
		"\3\2\2\2/\u00be\3\2\2\2\61\u00c0\3\2\2\2\63\u00c2\3\2\2\2\65\u00c4\3\2"+
		"\2\2\67\u00c6\3\2\2\29\u00c8\3\2\2\2;\u00ca\3\2\2\2=\u00d5\3\2\2\2?\u00df"+
		"\3\2\2\2A\u00e1\3\2\2\2C\u00e9\3\2\2\2E\u00f7\3\2\2\2G\u00fd\3\2\2\2I"+
		"J\7p\2\2JK\7w\2\2KL\7n\2\2LM\7n\2\2M\4\3\2\2\2NO\7k\2\2OP\7f\2\2P\6\3"+
		"\2\2\2QR\7c\2\2RS\7n\2\2ST\7k\2\2TU\7c\2\2UV\7u\2\2VW\7g\2\2WX\7u\2\2"+
		"X\b\3\2\2\2YZ\7u\2\2Z[\7v\2\2[\\\7c\2\2\\]\7t\2\2]^\7v\2\2^_\7k\2\2_`"+
		"\7f\2\2`a\7z\2\2a\n\3\2\2\2bc\7g\2\2cd\7p\2\2de\7f\2\2ef\7k\2\2fg\7f\2"+
		"\2gh\7z\2\2h\f\3\2\2\2ij\7r\2\2jk\7c\2\2kl\7t\2\2lm\7g\2\2mn\7p\2\2no"+
		"\7v\2\2o\16\3\2\2\2pq\7i\2\2qr\7t\2\2rs\7q\2\2st\7w\2\2tu\7r\2\2uv\7u"+
		"\2\2v\20\3\2\2\2wx\7c\2\2xy\7p\2\2yz\7e\2\2z{\7g\2\2{|\7u\2\2|}\7v\2\2"+
		"}~\7q\2\2~\177\7t\2\2\177\u0080\7u\2\2\u0080\22\3\2\2\2\u0081\u0082\7"+
		"x\2\2\u0082\u0083\7c\2\2\u0083\u0084\7n\2\2\u0084\u0085\7w\2\2\u0085\u0086"+
		"\7g\2\2\u0086\24\3\2\2\2\u0087\u008b\5%\23\2\u0088\u008a\n\2\2\2\u0089"+
		"\u0088\3\2\2\2\u008a\u008d\3\2\2\2\u008b\u0089\3\2\2\2\u008b\u008c\3\2"+
		"\2\2\u008c\u008e\3\2\2\2\u008d\u008b\3\2\2\2\u008e\u008f\5%\23\2\u008f"+
		"\26\3\2\2\2\u0090\u0094\5\'\24\2\u0091\u0093\n\3\2\2\u0092\u0091\3\2\2"+
		"\2\u0093\u0096\3\2\2\2\u0094\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095\u0097"+
		"\3\2\2\2\u0096\u0094\3\2\2\2\u0097\u0098\5\'\24\2\u0098\30\3\2\2\2\u0099"+
		"\u009a\7?\2\2\u009a\u00a7\7?\2\2\u009b\u009c\7#\2\2\u009c\u00a7\7?\2\2"+
		"\u009d\u009e\7@\2\2\u009e\u00a7\7?\2\2\u009f\u00a0\7>\2\2\u00a0\u00a7"+
		"\7?\2\2\u00a1\u00a7\t\4\2\2\u00a2\u00a3\7B\2\2\u00a3\u00a7\7B\2\2\u00a4"+
		"\u00a5\7#\2\2\u00a5\u00a7\7B\2\2\u00a6\u0099\3\2\2\2\u00a6\u009b\3\2\2"+
		"\2\u00a6\u009d\3\2\2\2\u00a6\u009f\3\2\2\2\u00a6\u00a1\3\2\2\2\u00a6\u00a2"+
		"\3\2\2\2\u00a6\u00a4\3\2\2\2\u00a7\32\3\2\2\2\u00a8\u00a9\7(\2\2\u00a9"+
		"\u00aa\7(\2\2\u00aa\34\3\2\2\2\u00ab\u00ac\7~\2\2\u00ac\u00ad\7~\2\2\u00ad"+
		"\36\3\2\2\2\u00ae\u00af\7#\2\2\u00af \3\2\2\2\u00b0\u00b1\7*\2\2\u00b1"+
		"\"\3\2\2\2\u00b2\u00b3\7+\2\2\u00b3$\3\2\2\2\u00b4\u00b5\7)\2\2\u00b5"+
		"&\3\2\2\2\u00b6\u00b7\7$\2\2\u00b7(\3\2\2\2\u00b8\u00b9\7&\2\2\u00b9*"+
		"\3\2\2\2\u00ba\u00bb\7\u0080\2\2\u00bb,\3\2\2\2\u00bc\u00bd\7]\2\2\u00bd"+
		".\3\2\2\2\u00be\u00bf\7_\2\2\u00bf\60\3\2\2\2\u00c0\u00c1\7.\2\2\u00c1"+
		"\62\3\2\2\2\u00c2\u00c3\7<\2\2\u00c3\64\3\2\2\2\u00c4\u00c5\7%\2\2\u00c5"+
		"\66\3\2\2\2\u00c6\u00c7\7/\2\2\u00c78\3\2\2\2\u00c8\u00c9\7\60\2\2\u00c9"+
		":\3\2\2\2\u00ca\u00cb\7a\2\2\u00cb<\3\2\2\2\u00cc\u00cd\7v\2\2\u00cd\u00ce"+
		"\7t\2\2\u00ce\u00cf\7w\2\2\u00cf\u00d6\7g\2\2\u00d0\u00d1\7h\2\2\u00d1"+
		"\u00d2\7c\2\2\u00d2\u00d3\7n\2\2\u00d3\u00d4\7u\2\2\u00d4\u00d6\7g\2\2"+
		"\u00d5\u00cc\3\2\2\2\u00d5\u00d0\3\2\2\2\u00d6>\3\2\2\2\u00d7\u00e0\7"+
		"\62\2\2\u00d8\u00dc\t\5\2\2\u00d9\u00db\t\6\2\2\u00da\u00d9\3\2\2\2\u00db"+
		"\u00de\3\2\2\2\u00dc\u00da\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00e0\3\2"+
		"\2\2\u00de\u00dc\3\2\2\2\u00df\u00d7\3\2\2\2\u00df\u00d8\3\2\2\2\u00e0"+
		"@\3\2\2\2\u00e1\u00e3\59\35\2\u00e2\u00e4\t\7\2\2\u00e3\u00e2\3\2\2\2"+
		"\u00e4\u00e5\3\2\2\2\u00e5\u00e3\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e6B\3"+
		"\2\2\2\u00e7\u00ea\5;\36\2\u00e8\u00ea\t\b\2\2\u00e9\u00e7\3\2\2\2\u00e9"+
		"\u00e8\3\2\2\2\u00ea\u00eb\3\2\2\2\u00eb\u00e9\3\2\2\2\u00eb\u00ec\3\2"+
		"\2\2\u00ec\u00f3\3\2\2\2\u00ed\u00f2\t\t\2\2\u00ee\u00f2\5\63\32\2\u00ef"+
		"\u00f2\5\67\34\2\u00f0\u00f2\5;\36\2\u00f1\u00ed\3\2\2\2\u00f1\u00ee\3"+
		"\2\2\2\u00f1\u00ef\3\2\2\2\u00f1\u00f0\3\2\2\2\u00f2\u00f5\3\2\2\2\u00f3"+
		"\u00f1\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4D\3\2\2\2\u00f5\u00f3\3\2\2\2"+
		"\u00f6\u00f8\t\n\2\2\u00f7\u00f6\3\2\2\2\u00f8\u00f9\3\2\2\2\u00f9\u00f7"+
		"\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\u00fb\3\2\2\2\u00fb\u00fc\b#\2\2\u00fc"+
		"F\3\2\2\2\u00fd\u00fe\13\2\2\2\u00feH\3\2\2\2\17\2\u008b\u0094\u00a6\u00d5"+
		"\u00dc\u00df\u00e5\u00e9\u00eb\u00f1\u00f3\u00f9\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
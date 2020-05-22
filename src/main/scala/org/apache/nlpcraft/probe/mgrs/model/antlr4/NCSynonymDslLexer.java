// Generated from /Users/xxx/nlpcraft/src/main/scala/org/apache/nlpcraft/probe/mgrs/model/antlr4/NCSynonymDsl.g4 by ANTLR 4.8
package org.apache.nlpcraft.probe.mgrs.model.antlr4;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCSynonymDslLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		PRED_OP=10, AND=11, OR=12, EXCL=13, LPAREN=14, RPAREN=15, SQUOTE=16, TILDA=17, 
		LBR=18, RBR=19, COMMA=20, COLON=21, MINUS=22, DOT=23, UNDERSCORE=24, BOOL=25, 
		INT=26, EXP=27, ID=28, WS=29, ErrorCharacter=30;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"PRED_OP", "AND", "OR", "EXCL", "LPAREN", "RPAREN", "SQUOTE", "TILDA", 
			"LBR", "RBR", "COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", "BOOL", 
			"INT", "EXP", "ID", "WS", "ErrorCharacter"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'id'", "'aliases'", "'startidx'", "'endidx'", "'parent'", "'groups'", 
			"'ancestors'", "'value'", "'null'", null, "'&&'", "'||'", "'!'", "'('", 
			"')'", "'''", "'~'", "'['", "']'", "','", "':'", "'-'", "'.'", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, "PRED_OP", 
			"AND", "OR", "EXCL", "LPAREN", "RPAREN", "SQUOTE", "TILDA", "LBR", "RBR", 
			"COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", "BOOL", "INT", "EXP", 
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2 \u00dd\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\3\2\3\2\3"+
		"\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3"+
		"\13\3\13\3\13\3\13\3\13\5\13\u008b\n\13\3\f\3\f\3\f\3\r\3\r\3\r\3\16\3"+
		"\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3"+
		"\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3"+
		"\32\3\32\3\32\3\32\5\32\u00b4\n\32\3\33\3\33\3\33\7\33\u00b9\n\33\f\33"+
		"\16\33\u00bc\13\33\5\33\u00be\n\33\3\34\3\34\6\34\u00c2\n\34\r\34\16\34"+
		"\u00c3\3\35\3\35\6\35\u00c8\n\35\r\35\16\35\u00c9\3\35\3\35\3\35\3\35"+
		"\7\35\u00d0\n\35\f\35\16\35\u00d3\13\35\3\36\6\36\u00d6\n\36\r\36\16\36"+
		"\u00d7\3\36\3\36\3\37\3\37\2\2 \3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23"+
		"\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31"+
		"\61\32\63\33\65\34\67\359\36;\37= \3\2\t\4\2>>@@\3\2\63;\4\2\62;aa\3\2"+
		"\62;\4\2C\\c|\5\2\62;C\\c|\5\2\13\f\16\17\"\"\2\u00ed\2\3\3\2\2\2\2\5"+
		"\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2"+
		"\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33"+
		"\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2"+
		"\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2"+
		"\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2"+
		"\3?\3\2\2\2\5B\3\2\2\2\7J\3\2\2\2\tS\3\2\2\2\13Z\3\2\2\2\ra\3\2\2\2\17"+
		"h\3\2\2\2\21r\3\2\2\2\23x\3\2\2\2\25\u008a\3\2\2\2\27\u008c\3\2\2\2\31"+
		"\u008f\3\2\2\2\33\u0092\3\2\2\2\35\u0094\3\2\2\2\37\u0096\3\2\2\2!\u0098"+
		"\3\2\2\2#\u009a\3\2\2\2%\u009c\3\2\2\2\'\u009e\3\2\2\2)\u00a0\3\2\2\2"+
		"+\u00a2\3\2\2\2-\u00a4\3\2\2\2/\u00a6\3\2\2\2\61\u00a8\3\2\2\2\63\u00b3"+
		"\3\2\2\2\65\u00bd\3\2\2\2\67\u00bf\3\2\2\29\u00c7\3\2\2\2;\u00d5\3\2\2"+
		"\2=\u00db\3\2\2\2?@\7k\2\2@A\7f\2\2A\4\3\2\2\2BC\7c\2\2CD\7n\2\2DE\7k"+
		"\2\2EF\7c\2\2FG\7u\2\2GH\7g\2\2HI\7u\2\2I\6\3\2\2\2JK\7u\2\2KL\7v\2\2"+
		"LM\7c\2\2MN\7t\2\2NO\7v\2\2OP\7k\2\2PQ\7f\2\2QR\7z\2\2R\b\3\2\2\2ST\7"+
		"g\2\2TU\7p\2\2UV\7f\2\2VW\7k\2\2WX\7f\2\2XY\7z\2\2Y\n\3\2\2\2Z[\7r\2\2"+
		"[\\\7c\2\2\\]\7t\2\2]^\7g\2\2^_\7p\2\2_`\7v\2\2`\f\3\2\2\2ab\7i\2\2bc"+
		"\7t\2\2cd\7q\2\2de\7w\2\2ef\7r\2\2fg\7u\2\2g\16\3\2\2\2hi\7c\2\2ij\7p"+
		"\2\2jk\7e\2\2kl\7g\2\2lm\7u\2\2mn\7v\2\2no\7q\2\2op\7t\2\2pq\7u\2\2q\20"+
		"\3\2\2\2rs\7x\2\2st\7c\2\2tu\7n\2\2uv\7w\2\2vw\7g\2\2w\22\3\2\2\2xy\7"+
		"p\2\2yz\7w\2\2z{\7n\2\2{|\7n\2\2|\24\3\2\2\2}~\7?\2\2~\u008b\7?\2\2\177"+
		"\u0080\7#\2\2\u0080\u008b\7?\2\2\u0081\u0082\7@\2\2\u0082\u008b\7?\2\2"+
		"\u0083\u0084\7>\2\2\u0084\u008b\7?\2\2\u0085\u008b\t\2\2\2\u0086\u0087"+
		"\7B\2\2\u0087\u008b\7B\2\2\u0088\u0089\7#\2\2\u0089\u008b\7B\2\2\u008a"+
		"}\3\2\2\2\u008a\177\3\2\2\2\u008a\u0081\3\2\2\2\u008a\u0083\3\2\2\2\u008a"+
		"\u0085\3\2\2\2\u008a\u0086\3\2\2\2\u008a\u0088\3\2\2\2\u008b\26\3\2\2"+
		"\2\u008c\u008d\7(\2\2\u008d\u008e\7(\2\2\u008e\30\3\2\2\2\u008f\u0090"+
		"\7~\2\2\u0090\u0091\7~\2\2\u0091\32\3\2\2\2\u0092\u0093\7#\2\2\u0093\34"+
		"\3\2\2\2\u0094\u0095\7*\2\2\u0095\36\3\2\2\2\u0096\u0097\7+\2\2\u0097"+
		" \3\2\2\2\u0098\u0099\7)\2\2\u0099\"\3\2\2\2\u009a\u009b\7\u0080\2\2\u009b"+
		"$\3\2\2\2\u009c\u009d\7]\2\2\u009d&\3\2\2\2\u009e\u009f\7_\2\2\u009f("+
		"\3\2\2\2\u00a0\u00a1\7.\2\2\u00a1*\3\2\2\2\u00a2\u00a3\7<\2\2\u00a3,\3"+
		"\2\2\2\u00a4\u00a5\7/\2\2\u00a5.\3\2\2\2\u00a6\u00a7\7\60\2\2\u00a7\60"+
		"\3\2\2\2\u00a8\u00a9\7a\2\2\u00a9\62\3\2\2\2\u00aa\u00ab\7v\2\2\u00ab"+
		"\u00ac\7t\2\2\u00ac\u00ad\7w\2\2\u00ad\u00b4\7g\2\2\u00ae\u00af\7h\2\2"+
		"\u00af\u00b0\7c\2\2\u00b0\u00b1\7n\2\2\u00b1\u00b2\7u\2\2\u00b2\u00b4"+
		"\7g\2\2\u00b3\u00aa\3\2\2\2\u00b3\u00ae\3\2\2\2\u00b4\64\3\2\2\2\u00b5"+
		"\u00be\7\62\2\2\u00b6\u00ba\t\3\2\2\u00b7\u00b9\t\4\2\2\u00b8\u00b7\3"+
		"\2\2\2\u00b9\u00bc\3\2\2\2\u00ba\u00b8\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb"+
		"\u00be\3\2\2\2\u00bc\u00ba\3\2\2\2\u00bd\u00b5\3\2\2\2\u00bd\u00b6\3\2"+
		"\2\2\u00be\66\3\2\2\2\u00bf\u00c1\5/\30\2\u00c0\u00c2\t\5\2\2\u00c1\u00c0"+
		"\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3\u00c1\3\2\2\2\u00c3\u00c4\3\2\2\2\u00c4"+
		"8\3\2\2\2\u00c5\u00c8\5\61\31\2\u00c6\u00c8\t\6\2\2\u00c7\u00c5\3\2\2"+
		"\2\u00c7\u00c6\3\2\2\2\u00c8\u00c9\3\2\2\2\u00c9\u00c7\3\2\2\2\u00c9\u00ca"+
		"\3\2\2\2\u00ca\u00d1\3\2\2\2\u00cb\u00d0\t\7\2\2\u00cc\u00d0\5+\26\2\u00cd"+
		"\u00d0\5-\27\2\u00ce\u00d0\5\61\31\2\u00cf\u00cb\3\2\2\2\u00cf\u00cc\3"+
		"\2\2\2\u00cf\u00cd\3\2\2\2\u00cf\u00ce\3\2\2\2\u00d0\u00d3\3\2\2\2\u00d1"+
		"\u00cf\3\2\2\2\u00d1\u00d2\3\2\2\2\u00d2:\3\2\2\2\u00d3\u00d1\3\2\2\2"+
		"\u00d4\u00d6\t\b\2\2\u00d5\u00d4\3\2\2\2\u00d6\u00d7\3\2\2\2\u00d7\u00d5"+
		"\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9\u00da\b\36\2\2"+
		"\u00da<\3\2\2\2\u00db\u00dc\13\2\2\2\u00dc>\3\2\2\2\r\2\u008a\u00b3\u00ba"+
		"\u00bd\u00c3\u00c7\u00c9\u00cf\u00d1\u00d7\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
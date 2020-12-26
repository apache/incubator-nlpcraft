// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4\NCIntentDsl.g4 by ANTLR 4.9
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
	static { RuntimeMetaData.checkVersion("4.9", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, PRED_OP=14, AND=15, OR=16, VERT=17, 
		EXCL=18, LPAREN=19, RPAREN=20, LCURLY=21, RCURLY=22, SQUOTE=23, TILDA=24, 
		RIGHT=25, LBR=26, RBR=27, COMMA=28, COLON=29, MINUS=30, DOT=31, UNDERSCORE=32, 
		EQ=33, PLUS=34, QUESTION=35, STAR=36, DOLLAR=37, POWER=38, BOOL=39, INT=40, 
		EXP=41, ID=42, WS=43, ErrorCharacter=44;
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
			"QUESTION", "STAR", "DOLLAR", "POWER", "BOOL", "INT", "EXP", "ID", "WS", 
			"ErrorCharacter"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'ordered'", "'flow'", "'term'", "'id'", "'aliases'", 
			"'startidx'", "'endidx'", "'parent'", "'groups'", "'ancestors'", "'value'", 
			"'null'", null, "'&&'", "'||'", "'|'", "'!'", "'('", "')'", "'{'", "'}'", 
			"'''", "'~'", "'>>'", "'['", "']'", "','", "':'", "'-'", "'.'", "'_'", 
			"'='", "'+'", "'?'", "'*'", "'$'", "'^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, "PRED_OP", "AND", "OR", "VERT", "EXCL", "LPAREN", "RPAREN", 
			"LCURLY", "RCURLY", "SQUOTE", "TILDA", "RIGHT", "LBR", "RBR", "COMMA", 
			"COLON", "MINUS", "DOT", "UNDERSCORE", "EQ", "PLUS", "QUESTION", "STAR", 
			"DOLLAR", "POWER", "BOOL", "INT", "EXP", "ID", "WS", "ErrorCharacter"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2.\u0127\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\3\16"+
		"\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\5\17\u00c0\n\17\3\20\3\20\3\20\3\21\3\21\3\21\3\22\3\22"+
		"\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31"+
		"\3\32\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3"+
		" \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3(\3(\3(\3(\3(\3"+
		"(\3(\5(\u00fe\n(\3)\3)\3)\7)\u0103\n)\f)\16)\u0106\13)\5)\u0108\n)\3*"+
		"\3*\6*\u010c\n*\r*\16*\u010d\3+\3+\6+\u0112\n+\r+\16+\u0113\3+\3+\3+\3"+
		"+\7+\u011a\n+\f+\16+\u011d\13+\3,\6,\u0120\n,\r,\16,\u0121\3,\3,\3-\3"+
		"-\2\2.\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17"+
		"\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\35"+
		"9\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.\3\2\t\4\2>>@@\3\2\63;\4\2\62"+
		";aa\3\2\62;\4\2C\\c|\5\2\62;C\\c|\5\2\13\f\16\17\"\"\2\u0137\2\3\3\2\2"+
		"\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3"+
		"\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2"+
		"\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2"+
		"\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2"+
		"\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3"+
		"\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2"+
		"\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2"+
		"W\3\2\2\2\2Y\3\2\2\2\3[\3\2\2\2\5b\3\2\2\2\7j\3\2\2\2\to\3\2\2\2\13t\3"+
		"\2\2\2\rw\3\2\2\2\17\177\3\2\2\2\21\u0088\3\2\2\2\23\u008f\3\2\2\2\25"+
		"\u0096\3\2\2\2\27\u009d\3\2\2\2\31\u00a7\3\2\2\2\33\u00ad\3\2\2\2\35\u00bf"+
		"\3\2\2\2\37\u00c1\3\2\2\2!\u00c4\3\2\2\2#\u00c7\3\2\2\2%\u00c9\3\2\2\2"+
		"\'\u00cb\3\2\2\2)\u00cd\3\2\2\2+\u00cf\3\2\2\2-\u00d1\3\2\2\2/\u00d3\3"+
		"\2\2\2\61\u00d5\3\2\2\2\63\u00d7\3\2\2\2\65\u00da\3\2\2\2\67\u00dc\3\2"+
		"\2\29\u00de\3\2\2\2;\u00e0\3\2\2\2=\u00e2\3\2\2\2?\u00e4\3\2\2\2A\u00e6"+
		"\3\2\2\2C\u00e8\3\2\2\2E\u00ea\3\2\2\2G\u00ec\3\2\2\2I\u00ee\3\2\2\2K"+
		"\u00f0\3\2\2\2M\u00f2\3\2\2\2O\u00fd\3\2\2\2Q\u0107\3\2\2\2S\u0109\3\2"+
		"\2\2U\u0111\3\2\2\2W\u011f\3\2\2\2Y\u0125\3\2\2\2[\\\7k\2\2\\]\7p\2\2"+
		"]^\7v\2\2^_\7g\2\2_`\7p\2\2`a\7v\2\2a\4\3\2\2\2bc\7q\2\2cd\7t\2\2de\7"+
		"f\2\2ef\7g\2\2fg\7t\2\2gh\7g\2\2hi\7f\2\2i\6\3\2\2\2jk\7h\2\2kl\7n\2\2"+
		"lm\7q\2\2mn\7y\2\2n\b\3\2\2\2op\7v\2\2pq\7g\2\2qr\7t\2\2rs\7o\2\2s\n\3"+
		"\2\2\2tu\7k\2\2uv\7f\2\2v\f\3\2\2\2wx\7c\2\2xy\7n\2\2yz\7k\2\2z{\7c\2"+
		"\2{|\7u\2\2|}\7g\2\2}~\7u\2\2~\16\3\2\2\2\177\u0080\7u\2\2\u0080\u0081"+
		"\7v\2\2\u0081\u0082\7c\2\2\u0082\u0083\7t\2\2\u0083\u0084\7v\2\2\u0084"+
		"\u0085\7k\2\2\u0085\u0086\7f\2\2\u0086\u0087\7z\2\2\u0087\20\3\2\2\2\u0088"+
		"\u0089\7g\2\2\u0089\u008a\7p\2\2\u008a\u008b\7f\2\2\u008b\u008c\7k\2\2"+
		"\u008c\u008d\7f\2\2\u008d\u008e\7z\2\2\u008e\22\3\2\2\2\u008f\u0090\7"+
		"r\2\2\u0090\u0091\7c\2\2\u0091\u0092\7t\2\2\u0092\u0093\7g\2\2\u0093\u0094"+
		"\7p\2\2\u0094\u0095\7v\2\2\u0095\24\3\2\2\2\u0096\u0097\7i\2\2\u0097\u0098"+
		"\7t\2\2\u0098\u0099\7q\2\2\u0099\u009a\7w\2\2\u009a\u009b\7r\2\2\u009b"+
		"\u009c\7u\2\2\u009c\26\3\2\2\2\u009d\u009e\7c\2\2\u009e\u009f\7p\2\2\u009f"+
		"\u00a0\7e\2\2\u00a0\u00a1\7g\2\2\u00a1\u00a2\7u\2\2\u00a2\u00a3\7v\2\2"+
		"\u00a3\u00a4\7q\2\2\u00a4\u00a5\7t\2\2\u00a5\u00a6\7u\2\2\u00a6\30\3\2"+
		"\2\2\u00a7\u00a8\7x\2\2\u00a8\u00a9\7c\2\2\u00a9\u00aa\7n\2\2\u00aa\u00ab"+
		"\7w\2\2\u00ab\u00ac\7g\2\2\u00ac\32\3\2\2\2\u00ad\u00ae\7p\2\2\u00ae\u00af"+
		"\7w\2\2\u00af\u00b0\7n\2\2\u00b0\u00b1\7n\2\2\u00b1\34\3\2\2\2\u00b2\u00b3"+
		"\7?\2\2\u00b3\u00c0\7?\2\2\u00b4\u00b5\7#\2\2\u00b5\u00c0\7?\2\2\u00b6"+
		"\u00b7\7@\2\2\u00b7\u00c0\7?\2\2\u00b8\u00b9\7>\2\2\u00b9\u00c0\7?\2\2"+
		"\u00ba\u00c0\t\2\2\2\u00bb\u00bc\7B\2\2\u00bc\u00c0\7B\2\2\u00bd\u00be"+
		"\7#\2\2\u00be\u00c0\7B\2\2\u00bf\u00b2\3\2\2\2\u00bf\u00b4\3\2\2\2\u00bf"+
		"\u00b6\3\2\2\2\u00bf\u00b8\3\2\2\2\u00bf\u00ba\3\2\2\2\u00bf\u00bb\3\2"+
		"\2\2\u00bf\u00bd\3\2\2\2\u00c0\36\3\2\2\2\u00c1\u00c2\7(\2\2\u00c2\u00c3"+
		"\7(\2\2\u00c3 \3\2\2\2\u00c4\u00c5\7~\2\2\u00c5\u00c6\7~\2\2\u00c6\"\3"+
		"\2\2\2\u00c7\u00c8\7~\2\2\u00c8$\3\2\2\2\u00c9\u00ca\7#\2\2\u00ca&\3\2"+
		"\2\2\u00cb\u00cc\7*\2\2\u00cc(\3\2\2\2\u00cd\u00ce\7+\2\2\u00ce*\3\2\2"+
		"\2\u00cf\u00d0\7}\2\2\u00d0,\3\2\2\2\u00d1\u00d2\7\177\2\2\u00d2.\3\2"+
		"\2\2\u00d3\u00d4\7)\2\2\u00d4\60\3\2\2\2\u00d5\u00d6\7\u0080\2\2\u00d6"+
		"\62\3\2\2\2\u00d7\u00d8\7@\2\2\u00d8\u00d9\7@\2\2\u00d9\64\3\2\2\2\u00da"+
		"\u00db\7]\2\2\u00db\66\3\2\2\2\u00dc\u00dd\7_\2\2\u00dd8\3\2\2\2\u00de"+
		"\u00df\7.\2\2\u00df:\3\2\2\2\u00e0\u00e1\7<\2\2\u00e1<\3\2\2\2\u00e2\u00e3"+
		"\7/\2\2\u00e3>\3\2\2\2\u00e4\u00e5\7\60\2\2\u00e5@\3\2\2\2\u00e6\u00e7"+
		"\7a\2\2\u00e7B\3\2\2\2\u00e8\u00e9\7?\2\2\u00e9D\3\2\2\2\u00ea\u00eb\7"+
		"-\2\2\u00ebF\3\2\2\2\u00ec\u00ed\7A\2\2\u00edH\3\2\2\2\u00ee\u00ef\7,"+
		"\2\2\u00efJ\3\2\2\2\u00f0\u00f1\7&\2\2\u00f1L\3\2\2\2\u00f2\u00f3\7`\2"+
		"\2\u00f3N\3\2\2\2\u00f4\u00f5\7v\2\2\u00f5\u00f6\7t\2\2\u00f6\u00f7\7"+
		"w\2\2\u00f7\u00fe\7g\2\2\u00f8\u00f9\7h\2\2\u00f9\u00fa\7c\2\2\u00fa\u00fb"+
		"\7n\2\2\u00fb\u00fc\7u\2\2\u00fc\u00fe\7g\2\2\u00fd\u00f4\3\2\2\2\u00fd"+
		"\u00f8\3\2\2\2\u00feP\3\2\2\2\u00ff\u0108\7\62\2\2\u0100\u0104\t\3\2\2"+
		"\u0101\u0103\t\4\2\2\u0102\u0101\3\2\2\2\u0103\u0106\3\2\2\2\u0104\u0102"+
		"\3\2\2\2\u0104\u0105\3\2\2\2\u0105\u0108\3\2\2\2\u0106\u0104\3\2\2\2\u0107"+
		"\u00ff\3\2\2\2\u0107\u0100\3\2\2\2\u0108R\3\2\2\2\u0109\u010b\5? \2\u010a"+
		"\u010c\t\5\2\2\u010b\u010a\3\2\2\2\u010c\u010d\3\2\2\2\u010d\u010b\3\2"+
		"\2\2\u010d\u010e\3\2\2\2\u010eT\3\2\2\2\u010f\u0112\5A!\2\u0110\u0112"+
		"\t\6\2\2\u0111\u010f\3\2\2\2\u0111\u0110\3\2\2\2\u0112\u0113\3\2\2\2\u0113"+
		"\u0111\3\2\2\2\u0113\u0114\3\2\2\2\u0114\u011b\3\2\2\2\u0115\u011a\t\7"+
		"\2\2\u0116\u011a\5;\36\2\u0117\u011a\5=\37\2\u0118\u011a\5A!\2\u0119\u0115"+
		"\3\2\2\2\u0119\u0116\3\2\2\2\u0119\u0117\3\2\2\2\u0119\u0118\3\2\2\2\u011a"+
		"\u011d\3\2\2\2\u011b\u0119\3\2\2\2\u011b\u011c\3\2\2\2\u011cV\3\2\2\2"+
		"\u011d\u011b\3\2\2\2\u011e\u0120\t\b\2\2\u011f\u011e\3\2\2\2\u0120\u0121"+
		"\3\2\2\2\u0121\u011f\3\2\2\2\u0121\u0122\3\2\2\2\u0122\u0123\3\2\2\2\u0123"+
		"\u0124\b,\2\2\u0124X\3\2\2\2\u0125\u0126\13\2\2\2\u0126Z\3\2\2\2\r\2\u00bf"+
		"\u00fd\u0104\u0107\u010d\u0111\u0113\u0119\u011b\u0121\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
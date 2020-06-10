// Generated from /Users/xxx/nlpcraft/src/main/scala/org/apache/nlpcraft/probe/mgrs/model/antlr4/NCSynonymDsl.g4 by ANTLR 4.8
package org.apache.nlpcraft.probe.mgrs.model.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCSynonymDslParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		PRED_OP=10, AND=11, OR=12, EXCL=13, LPAREN=14, RPAREN=15, SQUOTE=16, TILDA=17, 
		LBR=18, RBR=19, COMMA=20, COLON=21, MINUS=22, DOT=23, UNDERSCORE=24, BOOL=25, 
		INT=26, EXP=27, ID=28, WS=29, ErrorCharacter=30;
	public static final int
		RULE_synonym = 0, RULE_alias = 1, RULE_item = 2, RULE_predicate = 3, RULE_lval = 4, 
		RULE_lvalQual = 5, RULE_lvalPart = 6, RULE_rvalSingle = 7, RULE_rval = 8, 
		RULE_rvalList = 9, RULE_meta = 10, RULE_qstring = 11;
	private static String[] makeRuleNames() {
		return new String[] {
			"synonym", "alias", "item", "predicate", "lval", "lvalQual", "lvalPart", 
			"rvalSingle", "rval", "rvalList", "meta", "qstring"
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

	@Override
	public String getGrammarFileName() { return "NCSynonymDsl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public NCSynonymDslParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class SynonymContext extends ParserRuleContext {
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(NCSynonymDslParser.LPAREN, 0); }
		public ItemContext item() {
			return getRuleContext(ItemContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(NCSynonymDslParser.RPAREN, 0); }
		public TerminalNode EOF() { return getToken(NCSynonymDslParser.EOF, 0); }
		public SynonymContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_synonym; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterSynonym(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitSynonym(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitSynonym(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SynonymContext synonym() throws RecognitionException {
		SynonymContext _localctx = new SynonymContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_synonym);
		try {
			setState(33);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBR:
				enterOuterAlt(_localctx, 1);
				{
				setState(24);
				alias();
				setState(25);
				match(LPAREN);
				setState(26);
				item(0);
				setState(27);
				match(RPAREN);
				setState(28);
				match(EOF);
				}
				break;
			case T__0:
			case T__1:
			case T__2:
			case T__3:
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case EXCL:
			case LPAREN:
			case TILDA:
			case ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(30);
				item(0);
				setState(31);
				match(EOF);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AliasContext extends ParserRuleContext {
		public TerminalNode LBR() { return getToken(NCSynonymDslParser.LBR, 0); }
		public TerminalNode ID() { return getToken(NCSynonymDslParser.ID, 0); }
		public TerminalNode RBR() { return getToken(NCSynonymDslParser.RBR, 0); }
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitAlias(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitAlias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(35);
			match(LBR);
			setState(36);
			match(ID);
			setState(37);
			match(RBR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ItemContext extends ParserRuleContext {
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(NCSynonymDslParser.LPAREN, 0); }
		public List<ItemContext> item() {
			return getRuleContexts(ItemContext.class);
		}
		public ItemContext item(int i) {
			return getRuleContext(ItemContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(NCSynonymDslParser.RPAREN, 0); }
		public TerminalNode EXCL() { return getToken(NCSynonymDslParser.EXCL, 0); }
		public TerminalNode AND() { return getToken(NCSynonymDslParser.AND, 0); }
		public TerminalNode OR() { return getToken(NCSynonymDslParser.OR, 0); }
		public ItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ItemContext item() throws RecognitionException {
		return item(0);
	}

	private ItemContext item(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ItemContext _localctx = new ItemContext(_ctx, _parentState);
		ItemContext _prevctx = _localctx;
		int _startState = 4;
		enterRecursionRule(_localctx, 4, RULE_item, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case T__2:
			case T__3:
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case TILDA:
			case ID:
				{
				setState(40);
				predicate();
				}
				break;
			case LPAREN:
				{
				setState(41);
				match(LPAREN);
				setState(42);
				item(0);
				setState(43);
				match(RPAREN);
				}
				break;
			case EXCL:
				{
				setState(45);
				match(EXCL);
				setState(46);
				item(1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(54);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ItemContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_item);
					setState(49);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(50);
					_la = _input.LA(1);
					if ( !(_la==AND || _la==OR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(51);
					item(3);
					}
					} 
				}
				setState(56);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class PredicateContext extends ParserRuleContext {
		public LvalContext lval() {
			return getRuleContext(LvalContext.class,0);
		}
		public TerminalNode PRED_OP() { return getToken(NCSynonymDslParser.PRED_OP, 0); }
		public RvalContext rval() {
			return getRuleContext(RvalContext.class,0);
		}
		public TerminalNode ID() { return getToken(NCSynonymDslParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(NCSynonymDslParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(NCSynonymDslParser.RPAREN, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitPredicate(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_predicate);
		try {
			setState(68);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(57);
				lval();
				setState(58);
				match(PRED_OP);
				setState(59);
				rval();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(61);
				match(ID);
				setState(62);
				match(LPAREN);
				setState(63);
				lval();
				setState(64);
				match(RPAREN);
				setState(65);
				match(PRED_OP);
				setState(66);
				rval();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LvalContext extends ParserRuleContext {
		public MetaContext meta() {
			return getRuleContext(MetaContext.class,0);
		}
		public LvalQualContext lvalQual() {
			return getRuleContext(LvalQualContext.class,0);
		}
		public LvalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterLval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitLval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitLval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LvalContext lval() throws RecognitionException {
		LvalContext _localctx = new LvalContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_lval);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(70);
				lvalQual(0);
				}
			}

			setState(82);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				{
				setState(73);
				match(T__0);
				}
				break;
			case T__1:
				{
				setState(74);
				match(T__1);
				}
				break;
			case T__2:
				{
				setState(75);
				match(T__2);
				}
				break;
			case T__3:
				{
				setState(76);
				match(T__3);
				}
				break;
			case T__4:
				{
				setState(77);
				match(T__4);
				}
				break;
			case T__5:
				{
				setState(78);
				match(T__5);
				}
				break;
			case T__6:
				{
				setState(79);
				match(T__6);
				}
				break;
			case T__7:
				{
				setState(80);
				match(T__7);
				}
				break;
			case TILDA:
				{
				setState(81);
				meta();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LvalQualContext extends ParserRuleContext {
		public LvalPartContext lvalPart() {
			return getRuleContext(LvalPartContext.class,0);
		}
		public LvalQualContext lvalQual() {
			return getRuleContext(LvalQualContext.class,0);
		}
		public LvalQualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lvalQual; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterLvalQual(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitLvalQual(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitLvalQual(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LvalQualContext lvalQual() throws RecognitionException {
		return lvalQual(0);
	}

	private LvalQualContext lvalQual(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		LvalQualContext _localctx = new LvalQualContext(_ctx, _parentState);
		LvalQualContext _prevctx = _localctx;
		int _startState = 10;
		enterRecursionRule(_localctx, 10, RULE_lvalQual, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(85);
			lvalPart();
			}
			_ctx.stop = _input.LT(-1);
			setState(91);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new LvalQualContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_lvalQual);
					setState(87);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(88);
					lvalPart();
					}
					} 
				}
				setState(93);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class LvalPartContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(NCSynonymDslParser.ID, 0); }
		public TerminalNode DOT() { return getToken(NCSynonymDslParser.DOT, 0); }
		public LvalPartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lvalPart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterLvalPart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitLvalPart(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitLvalPart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LvalPartContext lvalPart() throws RecognitionException {
		LvalPartContext _localctx = new LvalPartContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_lvalPart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			match(ID);
			setState(95);
			match(DOT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RvalSingleContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(NCSynonymDslParser.INT, 0); }
		public TerminalNode EXP() { return getToken(NCSynonymDslParser.EXP, 0); }
		public TerminalNode MINUS() { return getToken(NCSynonymDslParser.MINUS, 0); }
		public TerminalNode BOOL() { return getToken(NCSynonymDslParser.BOOL, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public RvalSingleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rvalSingle; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterRvalSingle(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitRvalSingle(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitRvalSingle(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RvalSingleContext rvalSingle() throws RecognitionException {
		RvalSingleContext _localctx = new RvalSingleContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_rvalSingle);
		int _la;
		try {
			setState(108);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__8:
				enterOuterAlt(_localctx, 1);
				{
				setState(97);
				match(T__8);
				}
				break;
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(99);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(98);
					match(MINUS);
					}
				}

				setState(104);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
				case 1:
					{
					setState(101);
					match(INT);
					}
					break;
				case 2:
					{
					setState(102);
					match(INT);
					setState(103);
					match(EXP);
					}
					break;
				}
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(106);
				match(BOOL);
				}
				break;
			case SQUOTE:
				enterOuterAlt(_localctx, 4);
				{
				setState(107);
				qstring();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RvalContext extends ParserRuleContext {
		public RvalSingleContext rvalSingle() {
			return getRuleContext(RvalSingleContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(NCSynonymDslParser.LPAREN, 0); }
		public RvalListContext rvalList() {
			return getRuleContext(RvalListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(NCSynonymDslParser.RPAREN, 0); }
		public RvalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterRval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitRval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitRval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RvalContext rval() throws RecognitionException {
		RvalContext _localctx = new RvalContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_rval);
		try {
			setState(115);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__8:
			case SQUOTE:
			case MINUS:
			case BOOL:
			case INT:
				enterOuterAlt(_localctx, 1);
				{
				setState(110);
				rvalSingle();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(111);
				match(LPAREN);
				setState(112);
				rvalList(0);
				setState(113);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RvalListContext extends ParserRuleContext {
		public RvalSingleContext rvalSingle() {
			return getRuleContext(RvalSingleContext.class,0);
		}
		public RvalListContext rvalList() {
			return getRuleContext(RvalListContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(NCSynonymDslParser.COMMA, 0); }
		public RvalListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rvalList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterRvalList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitRvalList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitRvalList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RvalListContext rvalList() throws RecognitionException {
		return rvalList(0);
	}

	private RvalListContext rvalList(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		RvalListContext _localctx = new RvalListContext(_ctx, _parentState);
		RvalListContext _prevctx = _localctx;
		int _startState = 18;
		enterRecursionRule(_localctx, 18, RULE_rvalList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(118);
			rvalSingle();
			}
			_ctx.stop = _input.LT(-1);
			setState(125);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new RvalListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_rvalList);
					setState(120);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(121);
					match(COMMA);
					setState(122);
					rvalSingle();
					}
					} 
				}
				setState(127);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class MetaContext extends ParserRuleContext {
		public TerminalNode TILDA() { return getToken(NCSynonymDslParser.TILDA, 0); }
		public TerminalNode ID() { return getToken(NCSynonymDslParser.ID, 0); }
		public TerminalNode LBR() { return getToken(NCSynonymDslParser.LBR, 0); }
		public TerminalNode INT() { return getToken(NCSynonymDslParser.INT, 0); }
		public TerminalNode RBR() { return getToken(NCSynonymDslParser.RBR, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public MetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_meta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitMeta(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitMeta(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MetaContext meta() throws RecognitionException {
		MetaContext _localctx = new MetaContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_meta);
		try {
			setState(141);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(128);
				match(TILDA);
				setState(129);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(130);
				match(TILDA);
				setState(131);
				match(ID);
				setState(132);
				match(LBR);
				setState(133);
				match(INT);
				setState(134);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(135);
				match(TILDA);
				setState(136);
				match(ID);
				setState(137);
				match(LBR);
				setState(138);
				qstring();
				setState(139);
				match(RBR);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QstringContext extends ParserRuleContext {
		public List<TerminalNode> SQUOTE() { return getTokens(NCSynonymDslParser.SQUOTE); }
		public TerminalNode SQUOTE(int i) {
			return getToken(NCSynonymDslParser.SQUOTE, i);
		}
		public QstringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qstring; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterQstring(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitQstring(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCSynonymDslVisitor ) return ((NCSynonymDslVisitor<? extends T>)visitor).visitQstring(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QstringContext qstring() throws RecognitionException {
		QstringContext _localctx = new QstringContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			match(SQUOTE);
			setState(147);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << PRED_OP) | (1L << AND) | (1L << OR) | (1L << EXCL) | (1L << LPAREN) | (1L << RPAREN) | (1L << TILDA) | (1L << LBR) | (1L << RBR) | (1L << COMMA) | (1L << COLON) | (1L << MINUS) | (1L << DOT) | (1L << UNDERSCORE) | (1L << BOOL) | (1L << INT) | (1L << EXP) | (1L << ID) | (1L << WS) | (1L << ErrorCharacter))) != 0)) {
				{
				{
				setState(144);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==SQUOTE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(149);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(150);
			match(SQUOTE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 2:
			return item_sempred((ItemContext)_localctx, predIndex);
		case 5:
			return lvalQual_sempred((LvalQualContext)_localctx, predIndex);
		case 9:
			return rvalList_sempred((RvalListContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean item_sempred(ItemContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean lvalQual_sempred(LvalQualContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean rvalList_sempred(RvalListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3 \u009b\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\5\2$\n\2\3\3\3"+
		"\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4\62\n\4\3\4\3\4\3\4\7\4"+
		"\67\n\4\f\4\16\4:\13\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5"+
		"G\n\5\3\6\5\6J\n\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5\6U\n\6\3\7\3"+
		"\7\3\7\3\7\3\7\7\7\\\n\7\f\7\16\7_\13\7\3\b\3\b\3\b\3\t\3\t\5\tf\n\t\3"+
		"\t\3\t\3\t\5\tk\n\t\3\t\3\t\5\to\n\t\3\n\3\n\3\n\3\n\3\n\5\nv\n\n\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\7\13~\n\13\f\13\16\13\u0081\13\13\3\f\3\f\3"+
		"\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\5\f\u0090\n\f\3\r\3\r\7\r\u0094"+
		"\n\r\f\r\16\r\u0097\13\r\3\r\3\r\3\r\2\5\6\f\24\16\2\4\6\b\n\f\16\20\22"+
		"\24\26\30\2\4\3\2\r\16\3\2\22\22\2\u00a7\2#\3\2\2\2\4%\3\2\2\2\6\61\3"+
		"\2\2\2\bF\3\2\2\2\nI\3\2\2\2\fV\3\2\2\2\16`\3\2\2\2\20n\3\2\2\2\22u\3"+
		"\2\2\2\24w\3\2\2\2\26\u008f\3\2\2\2\30\u0091\3\2\2\2\32\33\5\4\3\2\33"+
		"\34\7\20\2\2\34\35\5\6\4\2\35\36\7\21\2\2\36\37\7\2\2\3\37$\3\2\2\2 !"+
		"\5\6\4\2!\"\7\2\2\3\"$\3\2\2\2#\32\3\2\2\2# \3\2\2\2$\3\3\2\2\2%&\7\24"+
		"\2\2&\'\7\36\2\2\'(\7\25\2\2(\5\3\2\2\2)*\b\4\1\2*\62\5\b\5\2+,\7\20\2"+
		"\2,-\5\6\4\2-.\7\21\2\2.\62\3\2\2\2/\60\7\17\2\2\60\62\5\6\4\3\61)\3\2"+
		"\2\2\61+\3\2\2\2\61/\3\2\2\2\628\3\2\2\2\63\64\f\4\2\2\64\65\t\2\2\2\65"+
		"\67\5\6\4\5\66\63\3\2\2\2\67:\3\2\2\28\66\3\2\2\289\3\2\2\29\7\3\2\2\2"+
		":8\3\2\2\2;<\5\n\6\2<=\7\f\2\2=>\5\22\n\2>G\3\2\2\2?@\7\36\2\2@A\7\20"+
		"\2\2AB\5\n\6\2BC\7\21\2\2CD\7\f\2\2DE\5\22\n\2EG\3\2\2\2F;\3\2\2\2F?\3"+
		"\2\2\2G\t\3\2\2\2HJ\5\f\7\2IH\3\2\2\2IJ\3\2\2\2JT\3\2\2\2KU\7\3\2\2LU"+
		"\7\4\2\2MU\7\5\2\2NU\7\6\2\2OU\7\7\2\2PU\7\b\2\2QU\7\t\2\2RU\7\n\2\2S"+
		"U\5\26\f\2TK\3\2\2\2TL\3\2\2\2TM\3\2\2\2TN\3\2\2\2TO\3\2\2\2TP\3\2\2\2"+
		"TQ\3\2\2\2TR\3\2\2\2TS\3\2\2\2U\13\3\2\2\2VW\b\7\1\2WX\5\16\b\2X]\3\2"+
		"\2\2YZ\f\3\2\2Z\\\5\16\b\2[Y\3\2\2\2\\_\3\2\2\2][\3\2\2\2]^\3\2\2\2^\r"+
		"\3\2\2\2_]\3\2\2\2`a\7\36\2\2ab\7\31\2\2b\17\3\2\2\2co\7\13\2\2df\7\30"+
		"\2\2ed\3\2\2\2ef\3\2\2\2fj\3\2\2\2gk\7\34\2\2hi\7\34\2\2ik\7\35\2\2jg"+
		"\3\2\2\2jh\3\2\2\2ko\3\2\2\2lo\7\33\2\2mo\5\30\r\2nc\3\2\2\2ne\3\2\2\2"+
		"nl\3\2\2\2nm\3\2\2\2o\21\3\2\2\2pv\5\20\t\2qr\7\20\2\2rs\5\24\13\2st\7"+
		"\21\2\2tv\3\2\2\2up\3\2\2\2uq\3\2\2\2v\23\3\2\2\2wx\b\13\1\2xy\5\20\t"+
		"\2y\177\3\2\2\2z{\f\3\2\2{|\7\26\2\2|~\5\20\t\2}z\3\2\2\2~\u0081\3\2\2"+
		"\2\177}\3\2\2\2\177\u0080\3\2\2\2\u0080\25\3\2\2\2\u0081\177\3\2\2\2\u0082"+
		"\u0083\7\23\2\2\u0083\u0090\7\36\2\2\u0084\u0085\7\23\2\2\u0085\u0086"+
		"\7\36\2\2\u0086\u0087\7\24\2\2\u0087\u0088\7\34\2\2\u0088\u0090\7\25\2"+
		"\2\u0089\u008a\7\23\2\2\u008a\u008b\7\36\2\2\u008b\u008c\7\24\2\2\u008c"+
		"\u008d\5\30\r\2\u008d\u008e\7\25\2\2\u008e\u0090\3\2\2\2\u008f\u0082\3"+
		"\2\2\2\u008f\u0084\3\2\2\2\u008f\u0089\3\2\2\2\u0090\27\3\2\2\2\u0091"+
		"\u0095\7\22\2\2\u0092\u0094\n\3\2\2\u0093\u0092\3\2\2\2\u0094\u0097\3"+
		"\2\2\2\u0095\u0093\3\2\2\2\u0095\u0096\3\2\2\2\u0096\u0098\3\2\2\2\u0097"+
		"\u0095\3\2\2\2\u0098\u0099\7\22\2\2\u0099\31\3\2\2\2\20#\618FIT]ejnu\177"+
		"\u008f\u0095";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
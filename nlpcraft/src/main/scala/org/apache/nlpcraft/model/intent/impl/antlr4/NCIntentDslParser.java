// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4\NCIntentDsl.g4 by ANTLR 4.8
package org.apache.nlpcraft.model.intent.impl.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCIntentDslParser extends Parser {
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
	public static final int
		RULE_intent = 0, RULE_intentId = 1, RULE_orderedDecl = 2, RULE_flowDecl = 3, 
		RULE_flow = 4, RULE_flowItem = 5, RULE_flowItemIds = 6, RULE_idList = 7, 
		RULE_terms = 8, RULE_termEq = 9, RULE_term = 10, RULE_termId = 11, RULE_item = 12, 
		RULE_predicate = 13, RULE_lval = 14, RULE_lvalQual = 15, RULE_lvalPart = 16, 
		RULE_rvalSingle = 17, RULE_rval = 18, RULE_rvalList = 19, RULE_meta = 20, 
		RULE_qstring = 21, RULE_minMax = 22, RULE_minMaxShortcut = 23, RULE_minMaxRange = 24;
	private static String[] makeRuleNames() {
		return new String[] {
			"intent", "intentId", "orderedDecl", "flowDecl", "flow", "flowItem", 
			"flowItemIds", "idList", "terms", "termEq", "term", "termId", "item", 
			"predicate", "lval", "lvalQual", "lvalPart", "rvalSingle", "rval", "rvalList", 
			"meta", "qstring", "minMax", "minMaxShortcut", "minMaxRange"
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

	@Override
	public String getGrammarFileName() { return "NCIntentDsl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public NCIntentDslParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class IntentContext extends ParserRuleContext {
		public IntentIdContext intentId() {
			return getRuleContext(IntentIdContext.class,0);
		}
		public TermsContext terms() {
			return getRuleContext(TermsContext.class,0);
		}
		public TerminalNode EOF() { return getToken(NCIntentDslParser.EOF, 0); }
		public OrderedDeclContext orderedDecl() {
			return getRuleContext(OrderedDeclContext.class,0);
		}
		public FlowDeclContext flowDecl() {
			return getRuleContext(FlowDeclContext.class,0);
		}
		public IntentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterIntent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitIntent(this);
		}
	}

	public final IntentContext intent() throws RecognitionException {
		IntentContext _localctx = new IntentContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_intent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			intentId();
			setState(52);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__1) {
				{
				setState(51);
				orderedDecl();
				}
			}

			setState(55);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(54);
				flowDecl();
				}
			}

			setState(57);
			terms(0);
			setState(58);
			match(EOF);
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

	public static class IntentIdContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public IntentIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intentId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterIntentId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitIntentId(this);
		}
	}

	public final IntentIdContext intentId() throws RecognitionException {
		IntentIdContext _localctx = new IntentIdContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_intentId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			match(T__0);
			setState(61);
			match(EQ);
			setState(62);
			match(ID);
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

	public static class OrderedDeclContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
		public TerminalNode BOOL() { return getToken(NCIntentDslParser.BOOL, 0); }
		public OrderedDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderedDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterOrderedDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitOrderedDecl(this);
		}
	}

	public final OrderedDeclContext orderedDecl() throws RecognitionException {
		OrderedDeclContext _localctx = new OrderedDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_orderedDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			match(T__1);
			setState(65);
			match(EQ);
			setState(66);
			match(BOOL);
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

	public static class FlowDeclContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
		public List<TerminalNode> SQUOTE() { return getTokens(NCIntentDslParser.SQUOTE); }
		public TerminalNode SQUOTE(int i) {
			return getToken(NCIntentDslParser.SQUOTE, i);
		}
		public FlowContext flow() {
			return getRuleContext(FlowContext.class,0);
		}
		public FlowDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flowDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterFlowDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitFlowDecl(this);
		}
	}

	public final FlowDeclContext flowDecl() throws RecognitionException {
		FlowDeclContext _localctx = new FlowDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_flowDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(68);
			match(T__2);
			setState(69);
			match(EQ);
			setState(70);
			match(SQUOTE);
			setState(71);
			flow(0);
			setState(72);
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

	public static class FlowContext extends ParserRuleContext {
		public FlowItemContext flowItem() {
			return getRuleContext(FlowItemContext.class,0);
		}
		public FlowContext flow() {
			return getRuleContext(FlowContext.class,0);
		}
		public TerminalNode RIGHT() { return getToken(NCIntentDslParser.RIGHT, 0); }
		public FlowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flow; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterFlow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitFlow(this);
		}
	}

	public final FlowContext flow() throws RecognitionException {
		return flow(0);
	}

	private FlowContext flow(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		FlowContext _localctx = new FlowContext(_ctx, _parentState);
		FlowContext _prevctx = _localctx;
		int _startState = 8;
		enterRecursionRule(_localctx, 8, RULE_flow, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				}
				break;
			case 2:
				{
				setState(75);
				flowItem();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(83);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new FlowContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_flow);
					setState(78);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(79);
					match(RIGHT);
					setState(80);
					flowItem();
					}
					} 
				}
				setState(85);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
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

	public static class FlowItemContext extends ParserRuleContext {
		public FlowItemIdsContext flowItemIds() {
			return getRuleContext(FlowItemIdsContext.class,0);
		}
		public MinMaxContext minMax() {
			return getRuleContext(MinMaxContext.class,0);
		}
		public FlowItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flowItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterFlowItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitFlowItem(this);
		}
	}

	public final FlowItemContext flowItem() throws RecognitionException {
		FlowItemContext _localctx = new FlowItemContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_flowItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			flowItemIds();
			setState(88);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(87);
				minMax();
				}
				break;
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

	public static class FlowItemIdsContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public FlowItemIdsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flowItemIds; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterFlowItemIds(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitFlowItemIds(this);
		}
	}

	public final FlowItemIdsContext flowItemIds() throws RecognitionException {
		FlowItemIdsContext _localctx = new FlowItemIdsContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_flowItemIds);
		try {
			setState(95);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(90);
				match(ID);
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(91);
				match(LPAREN);
				setState(92);
				idList(0);
				setState(93);
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

	public static class IdListContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public TerminalNode VERT() { return getToken(NCIntentDslParser.VERT, 0); }
		public IdListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterIdList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitIdList(this);
		}
	}

	public final IdListContext idList() throws RecognitionException {
		return idList(0);
	}

	private IdListContext idList(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		IdListContext _localctx = new IdListContext(_ctx, _parentState);
		IdListContext _prevctx = _localctx;
		int _startState = 14;
		enterRecursionRule(_localctx, 14, RULE_idList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(98);
			match(ID);
			}
			_ctx.stop = _input.LT(-1);
			setState(105);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new IdListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_idList);
					setState(100);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(101);
					match(VERT);
					setState(102);
					match(ID);
					}
					} 
				}
				setState(107);
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

	public static class TermsContext extends ParserRuleContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TermsContext terms() {
			return getRuleContext(TermsContext.class,0);
		}
		public TermsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_terms; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTerms(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTerms(this);
		}
	}

	public final TermsContext terms() throws RecognitionException {
		return terms(0);
	}

	private TermsContext terms(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TermsContext _localctx = new TermsContext(_ctx, _parentState);
		TermsContext _prevctx = _localctx;
		int _startState = 16;
		enterRecursionRule(_localctx, 16, RULE_terms, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(109);
			term();
			}
			_ctx.stop = _input.LT(-1);
			setState(115);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_terms);
					setState(111);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(112);
					term();
					}
					} 
				}
				setState(117);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
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

	public static class TermEqContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
		public TerminalNode TILDA() { return getToken(NCIntentDslParser.TILDA, 0); }
		public TermEqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termEq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTermEq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTermEq(this);
		}
	}

	public final TermEqContext termEq() throws RecognitionException {
		TermEqContext _localctx = new TermEqContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_termEq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(118);
			_la = _input.LA(1);
			if ( !(_la==TILDA || _la==EQ) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static class TermContext extends ParserRuleContext {
		public TermEqContext termEq() {
			return getRuleContext(TermEqContext.class,0);
		}
		public TerminalNode LCURLY() { return getToken(NCIntentDslParser.LCURLY, 0); }
		public ItemContext item() {
			return getRuleContext(ItemContext.class,0);
		}
		public TerminalNode RCURLY() { return getToken(NCIntentDslParser.RCURLY, 0); }
		public TermIdContext termId() {
			return getRuleContext(TermIdContext.class,0);
		}
		public MinMaxContext minMax() {
			return getRuleContext(MinMaxContext.class,0);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(120);
			match(T__3);
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(121);
				termId();
				}
			}

			setState(124);
			termEq();
			setState(125);
			match(LCURLY);
			setState(126);
			item(0);
			setState(127);
			match(RCURLY);
			setState(129);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(128);
				minMax();
				}
				break;
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

	public static class TermIdContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public TermIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTermId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTermId(this);
		}
	}

	public final TermIdContext termId() throws RecognitionException {
		TermIdContext _localctx = new TermIdContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_termId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			match(LPAREN);
			setState(132);
			match(ID);
			setState(133);
			match(RPAREN);
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
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public List<ItemContext> item() {
			return getRuleContexts(ItemContext.class);
		}
		public ItemContext item(int i) {
			return getRuleContext(ItemContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public TerminalNode EXCL() { return getToken(NCIntentDslParser.EXCL, 0); }
		public TerminalNode AND() { return getToken(NCIntentDslParser.AND, 0); }
		public TerminalNode OR() { return getToken(NCIntentDslParser.OR, 0); }
		public ItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitItem(this);
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
		int _startState = 24;
		enterRecursionRule(_localctx, 24, RULE_item, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__10:
			case T__11:
			case TILDA:
			case ID:
				{
				setState(136);
				predicate();
				}
				break;
			case LPAREN:
				{
				setState(137);
				match(LPAREN);
				setState(138);
				item(0);
				setState(139);
				match(RPAREN);
				}
				break;
			case EXCL:
				{
				setState(141);
				match(EXCL);
				setState(142);
				item(1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(150);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ItemContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_item);
					setState(145);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(146);
					_la = _input.LA(1);
					if ( !(_la==AND || _la==OR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(147);
					item(3);
					}
					} 
				}
				setState(152);
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

	public static class PredicateContext extends ParserRuleContext {
		public LvalContext lval() {
			return getRuleContext(LvalContext.class,0);
		}
		public TerminalNode PRED_OP() { return getToken(NCIntentDslParser.PRED_OP, 0); }
		public RvalContext rval() {
			return getRuleContext(RvalContext.class,0);
		}
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitPredicate(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_predicate);
		try {
			setState(164);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(153);
				lval();
				setState(154);
				match(PRED_OP);
				setState(155);
				rval();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(157);
				match(ID);
				setState(158);
				match(LPAREN);
				setState(159);
				lval();
				setState(160);
				match(RPAREN);
				setState(161);
				match(PRED_OP);
				setState(162);
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
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterLval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitLval(this);
		}
	}

	public final LvalContext lval() throws RecognitionException {
		LvalContext _localctx = new LvalContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_lval);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(166);
				lvalQual(0);
				}
			}

			setState(178);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
				{
				setState(169);
				match(T__4);
				}
				break;
			case T__5:
				{
				setState(170);
				match(T__5);
				}
				break;
			case T__6:
				{
				setState(171);
				match(T__6);
				}
				break;
			case T__7:
				{
				setState(172);
				match(T__7);
				}
				break;
			case T__8:
				{
				setState(173);
				match(T__8);
				}
				break;
			case T__9:
				{
				setState(174);
				match(T__9);
				}
				break;
			case T__10:
				{
				setState(175);
				match(T__10);
				}
				break;
			case T__11:
				{
				setState(176);
				match(T__11);
				}
				break;
			case TILDA:
				{
				setState(177);
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
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterLvalQual(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitLvalQual(this);
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
		int _startState = 30;
		enterRecursionRule(_localctx, 30, RULE_lvalQual, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(181);
			lvalPart();
			}
			_ctx.stop = _input.LT(-1);
			setState(187);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new LvalQualContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_lvalQual);
					setState(183);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(184);
					lvalPart();
					}
					} 
				}
				setState(189);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
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
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode DOT() { return getToken(NCIntentDslParser.DOT, 0); }
		public LvalPartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lvalPart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterLvalPart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitLvalPart(this);
		}
	}

	public final LvalPartContext lvalPart() throws RecognitionException {
		LvalPartContext _localctx = new LvalPartContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_lvalPart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			match(ID);
			setState(191);
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
		public TerminalNode INT() { return getToken(NCIntentDslParser.INT, 0); }
		public TerminalNode EXP() { return getToken(NCIntentDslParser.EXP, 0); }
		public TerminalNode MINUS() { return getToken(NCIntentDslParser.MINUS, 0); }
		public TerminalNode BOOL() { return getToken(NCIntentDslParser.BOOL, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public RvalSingleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rvalSingle; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterRvalSingle(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitRvalSingle(this);
		}
	}

	public final RvalSingleContext rvalSingle() throws RecognitionException {
		RvalSingleContext _localctx = new RvalSingleContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_rvalSingle);
		int _la;
		try {
			setState(204);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__12:
				enterOuterAlt(_localctx, 1);
				{
				setState(193);
				match(T__12);
				}
				break;
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(195);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(194);
					match(MINUS);
					}
				}

				setState(200);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
				case 1:
					{
					setState(197);
					match(INT);
					}
					break;
				case 2:
					{
					setState(198);
					match(INT);
					setState(199);
					match(EXP);
					}
					break;
				}
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(202);
				match(BOOL);
				}
				break;
			case SQUOTE:
				enterOuterAlt(_localctx, 4);
				{
				setState(203);
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
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public RvalListContext rvalList() {
			return getRuleContext(RvalListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public RvalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterRval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitRval(this);
		}
	}

	public final RvalContext rval() throws RecognitionException {
		RvalContext _localctx = new RvalContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_rval);
		try {
			setState(211);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__12:
			case SQUOTE:
			case MINUS:
			case BOOL:
			case INT:
				enterOuterAlt(_localctx, 1);
				{
				setState(206);
				rvalSingle();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(207);
				match(LPAREN);
				setState(208);
				rvalList(0);
				setState(209);
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
		public TerminalNode COMMA() { return getToken(NCIntentDslParser.COMMA, 0); }
		public RvalListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rvalList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterRvalList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitRvalList(this);
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
		int _startState = 38;
		enterRecursionRule(_localctx, 38, RULE_rvalList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(214);
			rvalSingle();
			}
			_ctx.stop = _input.LT(-1);
			setState(221);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new RvalListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_rvalList);
					setState(216);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(217);
					match(COMMA);
					setState(218);
					rvalSingle();
					}
					} 
				}
				setState(223);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
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
		public TerminalNode TILDA() { return getToken(NCIntentDslParser.TILDA, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode LBR() { return getToken(NCIntentDslParser.LBR, 0); }
		public TerminalNode INT() { return getToken(NCIntentDslParser.INT, 0); }
		public TerminalNode RBR() { return getToken(NCIntentDslParser.RBR, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public MetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_meta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMeta(this);
		}
	}

	public final MetaContext meta() throws RecognitionException {
		MetaContext _localctx = new MetaContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_meta);
		try {
			setState(237);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(224);
				match(TILDA);
				setState(225);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(226);
				match(TILDA);
				setState(227);
				match(ID);
				setState(228);
				match(LBR);
				setState(229);
				match(INT);
				setState(230);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(231);
				match(TILDA);
				setState(232);
				match(ID);
				setState(233);
				match(LBR);
				setState(234);
				qstring();
				setState(235);
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
		public List<TerminalNode> SQUOTE() { return getTokens(NCIntentDslParser.SQUOTE); }
		public TerminalNode SQUOTE(int i) {
			return getToken(NCIntentDslParser.SQUOTE, i);
		}
		public QstringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qstring; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterQstring(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitQstring(this);
		}
	}

	public final QstringContext qstring() throws RecognitionException {
		QstringContext _localctx = new QstringContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(239);
			match(SQUOTE);
			setState(243);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << PRED_OP) | (1L << AND) | (1L << OR) | (1L << VERT) | (1L << EXCL) | (1L << LPAREN) | (1L << RPAREN) | (1L << LCURLY) | (1L << RCURLY) | (1L << TILDA) | (1L << RIGHT) | (1L << LBR) | (1L << RBR) | (1L << COMMA) | (1L << COLON) | (1L << MINUS) | (1L << DOT) | (1L << UNDERSCORE) | (1L << EQ) | (1L << PLUS) | (1L << QUESTION) | (1L << STAR) | (1L << BOOL) | (1L << INT) | (1L << EXP) | (1L << ID) | (1L << WS) | (1L << ErrorCharacter))) != 0)) {
				{
				{
				setState(240);
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
				setState(245);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(246);
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

	public static class MinMaxContext extends ParserRuleContext {
		public MinMaxShortcutContext minMaxShortcut() {
			return getRuleContext(MinMaxShortcutContext.class,0);
		}
		public MinMaxRangeContext minMaxRange() {
			return getRuleContext(MinMaxRangeContext.class,0);
		}
		public MinMaxContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMax; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMinMax(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMinMax(this);
		}
	}

	public final MinMaxContext minMax() throws RecognitionException {
		MinMaxContext _localctx = new MinMaxContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_minMax);
		try {
			setState(250);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUS:
			case QUESTION:
			case STAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(248);
				minMaxShortcut();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 2);
				{
				setState(249);
				minMaxRange();
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

	public static class MinMaxShortcutContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(NCIntentDslParser.PLUS, 0); }
		public TerminalNode QUESTION() { return getToken(NCIntentDslParser.QUESTION, 0); }
		public TerminalNode STAR() { return getToken(NCIntentDslParser.STAR, 0); }
		public MinMaxShortcutContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMaxShortcut; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMinMaxShortcut(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMinMaxShortcut(this);
		}
	}

	public final MinMaxShortcutContext minMaxShortcut() throws RecognitionException {
		MinMaxShortcutContext _localctx = new MinMaxShortcutContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_minMaxShortcut);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(252);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << PLUS) | (1L << QUESTION) | (1L << STAR))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
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

	public static class MinMaxRangeContext extends ParserRuleContext {
		public TerminalNode LBR() { return getToken(NCIntentDslParser.LBR, 0); }
		public List<TerminalNode> INT() { return getTokens(NCIntentDslParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(NCIntentDslParser.INT, i);
		}
		public TerminalNode COMMA() { return getToken(NCIntentDslParser.COMMA, 0); }
		public TerminalNode RBR() { return getToken(NCIntentDslParser.RBR, 0); }
		public MinMaxRangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMaxRange; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMinMaxRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMinMaxRange(this);
		}
	}

	public final MinMaxRangeContext minMaxRange() throws RecognitionException {
		MinMaxRangeContext _localctx = new MinMaxRangeContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_minMaxRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(254);
			match(LBR);
			setState(255);
			match(INT);
			setState(256);
			match(COMMA);
			setState(257);
			match(INT);
			setState(258);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 4:
			return flow_sempred((FlowContext)_localctx, predIndex);
		case 7:
			return idList_sempred((IdListContext)_localctx, predIndex);
		case 8:
			return terms_sempred((TermsContext)_localctx, predIndex);
		case 12:
			return item_sempred((ItemContext)_localctx, predIndex);
		case 15:
			return lvalQual_sempred((LvalQualContext)_localctx, predIndex);
		case 19:
			return rvalList_sempred((RvalListContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean flow_sempred(FlowContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean idList_sempred(IdListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean terms_sempred(TermsContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean item_sempred(ItemContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean lvalQual_sempred(LvalQualContext _localctx, int predIndex) {
		switch (predIndex) {
		case 4:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean rvalList_sempred(RvalListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3,\u0107\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\3\2\3\2\5\2\67\n\2\3\2\5\2:\n\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3"+
		"\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\5\6O\n\6\3\6\3\6\3\6"+
		"\7\6T\n\6\f\6\16\6W\13\6\3\7\3\7\5\7[\n\7\3\b\3\b\3\b\3\b\3\b\5\bb\n\b"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\7\tj\n\t\f\t\16\tm\13\t\3\n\3\n\3\n\3\n\3\n\7"+
		"\nt\n\n\f\n\16\nw\13\n\3\13\3\13\3\f\3\f\5\f}\n\f\3\f\3\f\3\f\3\f\3\f"+
		"\5\f\u0084\n\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16"+
		"\5\16\u0092\n\16\3\16\3\16\3\16\7\16\u0097\n\16\f\16\16\16\u009a\13\16"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\5\17\u00a7\n\17"+
		"\3\20\5\20\u00aa\n\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\5\20"+
		"\u00b5\n\20\3\21\3\21\3\21\3\21\3\21\7\21\u00bc\n\21\f\21\16\21\u00bf"+
		"\13\21\3\22\3\22\3\22\3\23\3\23\5\23\u00c6\n\23\3\23\3\23\3\23\5\23\u00cb"+
		"\n\23\3\23\3\23\5\23\u00cf\n\23\3\24\3\24\3\24\3\24\3\24\5\24\u00d6\n"+
		"\24\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u00de\n\25\f\25\16\25\u00e1\13"+
		"\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\5"+
		"\26\u00f0\n\26\3\27\3\27\7\27\u00f4\n\27\f\27\16\27\u00f7\13\27\3\27\3"+
		"\27\3\30\3\30\5\30\u00fd\n\30\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32"+
		"\3\32\2\b\n\20\22\32 (\33\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&"+
		"(*,.\60\62\2\6\4\2\32\32##\3\2\21\22\3\2\31\31\3\2$&\2\u0110\2\64\3\2"+
		"\2\2\4>\3\2\2\2\6B\3\2\2\2\bF\3\2\2\2\nN\3\2\2\2\fX\3\2\2\2\16a\3\2\2"+
		"\2\20c\3\2\2\2\22n\3\2\2\2\24x\3\2\2\2\26z\3\2\2\2\30\u0085\3\2\2\2\32"+
		"\u0091\3\2\2\2\34\u00a6\3\2\2\2\36\u00a9\3\2\2\2 \u00b6\3\2\2\2\"\u00c0"+
		"\3\2\2\2$\u00ce\3\2\2\2&\u00d5\3\2\2\2(\u00d7\3\2\2\2*\u00ef\3\2\2\2,"+
		"\u00f1\3\2\2\2.\u00fc\3\2\2\2\60\u00fe\3\2\2\2\62\u0100\3\2\2\2\64\66"+
		"\5\4\3\2\65\67\5\6\4\2\66\65\3\2\2\2\66\67\3\2\2\2\679\3\2\2\28:\5\b\5"+
		"\298\3\2\2\29:\3\2\2\2:;\3\2\2\2;<\5\22\n\2<=\7\2\2\3=\3\3\2\2\2>?\7\3"+
		"\2\2?@\7#\2\2@A\7*\2\2A\5\3\2\2\2BC\7\4\2\2CD\7#\2\2DE\7\'\2\2E\7\3\2"+
		"\2\2FG\7\5\2\2GH\7#\2\2HI\7\31\2\2IJ\5\n\6\2JK\7\31\2\2K\t\3\2\2\2LO\b"+
		"\6\1\2MO\5\f\7\2NL\3\2\2\2NM\3\2\2\2OU\3\2\2\2PQ\f\3\2\2QR\7\33\2\2RT"+
		"\5\f\7\2SP\3\2\2\2TW\3\2\2\2US\3\2\2\2UV\3\2\2\2V\13\3\2\2\2WU\3\2\2\2"+
		"XZ\5\16\b\2Y[\5.\30\2ZY\3\2\2\2Z[\3\2\2\2[\r\3\2\2\2\\b\7*\2\2]^\7\25"+
		"\2\2^_\5\20\t\2_`\7\26\2\2`b\3\2\2\2a\\\3\2\2\2a]\3\2\2\2b\17\3\2\2\2"+
		"cd\b\t\1\2de\7*\2\2ek\3\2\2\2fg\f\3\2\2gh\7\23\2\2hj\7*\2\2if\3\2\2\2"+
		"jm\3\2\2\2ki\3\2\2\2kl\3\2\2\2l\21\3\2\2\2mk\3\2\2\2no\b\n\1\2op\5\26"+
		"\f\2pu\3\2\2\2qr\f\3\2\2rt\5\26\f\2sq\3\2\2\2tw\3\2\2\2us\3\2\2\2uv\3"+
		"\2\2\2v\23\3\2\2\2wu\3\2\2\2xy\t\2\2\2y\25\3\2\2\2z|\7\6\2\2{}\5\30\r"+
		"\2|{\3\2\2\2|}\3\2\2\2}~\3\2\2\2~\177\5\24\13\2\177\u0080\7\27\2\2\u0080"+
		"\u0081\5\32\16\2\u0081\u0083\7\30\2\2\u0082\u0084\5.\30\2\u0083\u0082"+
		"\3\2\2\2\u0083\u0084\3\2\2\2\u0084\27\3\2\2\2\u0085\u0086\7\25\2\2\u0086"+
		"\u0087\7*\2\2\u0087\u0088\7\26\2\2\u0088\31\3\2\2\2\u0089\u008a\b\16\1"+
		"\2\u008a\u0092\5\34\17\2\u008b\u008c\7\25\2\2\u008c\u008d\5\32\16\2\u008d"+
		"\u008e\7\26\2\2\u008e\u0092\3\2\2\2\u008f\u0090\7\24\2\2\u0090\u0092\5"+
		"\32\16\3\u0091\u0089\3\2\2\2\u0091\u008b\3\2\2\2\u0091\u008f\3\2\2\2\u0092"+
		"\u0098\3\2\2\2\u0093\u0094\f\4\2\2\u0094\u0095\t\3\2\2\u0095\u0097\5\32"+
		"\16\5\u0096\u0093\3\2\2\2\u0097\u009a\3\2\2\2\u0098\u0096\3\2\2\2\u0098"+
		"\u0099\3\2\2\2\u0099\33\3\2\2\2\u009a\u0098\3\2\2\2\u009b\u009c\5\36\20"+
		"\2\u009c\u009d\7\20\2\2\u009d\u009e\5&\24\2\u009e\u00a7\3\2\2\2\u009f"+
		"\u00a0\7*\2\2\u00a0\u00a1\7\25\2\2\u00a1\u00a2\5\36\20\2\u00a2\u00a3\7"+
		"\26\2\2\u00a3\u00a4\7\20\2\2\u00a4\u00a5\5&\24\2\u00a5\u00a7\3\2\2\2\u00a6"+
		"\u009b\3\2\2\2\u00a6\u009f\3\2\2\2\u00a7\35\3\2\2\2\u00a8\u00aa\5 \21"+
		"\2\u00a9\u00a8\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00b4\3\2\2\2\u00ab\u00b5"+
		"\7\7\2\2\u00ac\u00b5\7\b\2\2\u00ad\u00b5\7\t\2\2\u00ae\u00b5\7\n\2\2\u00af"+
		"\u00b5\7\13\2\2\u00b0\u00b5\7\f\2\2\u00b1\u00b5\7\r\2\2\u00b2\u00b5\7"+
		"\16\2\2\u00b3\u00b5\5*\26\2\u00b4\u00ab\3\2\2\2\u00b4\u00ac\3\2\2\2\u00b4"+
		"\u00ad\3\2\2\2\u00b4\u00ae\3\2\2\2\u00b4\u00af\3\2\2\2\u00b4\u00b0\3\2"+
		"\2\2\u00b4\u00b1\3\2\2\2\u00b4\u00b2\3\2\2\2\u00b4\u00b3\3\2\2\2\u00b5"+
		"\37\3\2\2\2\u00b6\u00b7\b\21\1\2\u00b7\u00b8\5\"\22\2\u00b8\u00bd\3\2"+
		"\2\2\u00b9\u00ba\f\3\2\2\u00ba\u00bc\5\"\22\2\u00bb\u00b9\3\2\2\2\u00bc"+
		"\u00bf\3\2\2\2\u00bd\u00bb\3\2\2\2\u00bd\u00be\3\2\2\2\u00be!\3\2\2\2"+
		"\u00bf\u00bd\3\2\2\2\u00c0\u00c1\7*\2\2\u00c1\u00c2\7!\2\2\u00c2#\3\2"+
		"\2\2\u00c3\u00cf\7\17\2\2\u00c4\u00c6\7 \2\2\u00c5\u00c4\3\2\2\2\u00c5"+
		"\u00c6\3\2\2\2\u00c6\u00ca\3\2\2\2\u00c7\u00cb\7(\2\2\u00c8\u00c9\7(\2"+
		"\2\u00c9\u00cb\7)\2\2\u00ca\u00c7\3\2\2\2\u00ca\u00c8\3\2\2\2\u00cb\u00cf"+
		"\3\2\2\2\u00cc\u00cf\7\'\2\2\u00cd\u00cf\5,\27\2\u00ce\u00c3\3\2\2\2\u00ce"+
		"\u00c5\3\2\2\2\u00ce\u00cc\3\2\2\2\u00ce\u00cd\3\2\2\2\u00cf%\3\2\2\2"+
		"\u00d0\u00d6\5$\23\2\u00d1\u00d2\7\25\2\2\u00d2\u00d3\5(\25\2\u00d3\u00d4"+
		"\7\26\2\2\u00d4\u00d6\3\2\2\2\u00d5\u00d0\3\2\2\2\u00d5\u00d1\3\2\2\2"+
		"\u00d6\'\3\2\2\2\u00d7\u00d8\b\25\1\2\u00d8\u00d9\5$\23\2\u00d9\u00df"+
		"\3\2\2\2\u00da\u00db\f\3\2\2\u00db\u00dc\7\36\2\2\u00dc\u00de\5$\23\2"+
		"\u00dd\u00da\3\2\2\2\u00de\u00e1\3\2\2\2\u00df\u00dd\3\2\2\2\u00df\u00e0"+
		"\3\2\2\2\u00e0)\3\2\2\2\u00e1\u00df\3\2\2\2\u00e2\u00e3\7\32\2\2\u00e3"+
		"\u00f0\7*\2\2\u00e4\u00e5\7\32\2\2\u00e5\u00e6\7*\2\2\u00e6\u00e7\7\34"+
		"\2\2\u00e7\u00e8\7(\2\2\u00e8\u00f0\7\35\2\2\u00e9\u00ea\7\32\2\2\u00ea"+
		"\u00eb\7*\2\2\u00eb\u00ec\7\34\2\2\u00ec\u00ed\5,\27\2\u00ed\u00ee\7\35"+
		"\2\2\u00ee\u00f0\3\2\2\2\u00ef\u00e2\3\2\2\2\u00ef\u00e4\3\2\2\2\u00ef"+
		"\u00e9\3\2\2\2\u00f0+\3\2\2\2\u00f1\u00f5\7\31\2\2\u00f2\u00f4\n\4\2\2"+
		"\u00f3\u00f2\3\2\2\2\u00f4\u00f7\3\2\2\2\u00f5\u00f3\3\2\2\2\u00f5\u00f6"+
		"\3\2\2\2\u00f6\u00f8\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f8\u00f9\7\31\2\2"+
		"\u00f9-\3\2\2\2\u00fa\u00fd\5\60\31\2\u00fb\u00fd\5\62\32\2\u00fc\u00fa"+
		"\3\2\2\2\u00fc\u00fb\3\2\2\2\u00fd/\3\2\2\2\u00fe\u00ff\t\5\2\2\u00ff"+
		"\61\3\2\2\2\u0100\u0101\7\34\2\2\u0101\u0102\7(\2\2\u0102\u0103\7\36\2"+
		"\2\u0103\u0104\7(\2\2\u0104\u0105\7\35\2\2\u0105\63\3\2\2\2\32\669NUZ"+
		"aku|\u0083\u0091\u0098\u00a6\u00a9\u00b4\u00bd\u00c5\u00ca\u00ce\u00d5"+
		"\u00df\u00ef\u00f5\u00fc";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
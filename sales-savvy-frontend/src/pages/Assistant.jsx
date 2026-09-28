import { useEffect, useRef, useState } from 'react'
import api, { extractError } from '../api/axios'
import { ErrorAlert, Layout } from '../components/Layout'

const suggestions = [
  'Help me find a thoughtful gift',
  'What can I get for under ₹1,000?',
  'Recommend something for my home',
]

export default function Assistant() {
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      content: 'Hi! I’m Sage, your shopping concierge. Tell me what you’re looking for, who it’s for, or the budget you have in mind.',
    },
  ])
  const [input, setInput] = useState('')
  const [error, setError] = useState('')
  const [sending, setSending] = useState(false)
  const conversationEnd = useRef(null)

  useEffect(() => {
    conversationEnd.current?.scrollIntoView({ behavior: 'smooth', block: 'end' })
  }, [messages, sending])

  async function sendMessage(text = input) {
    const message = text.trim()
    if (!message || sending) return

    const history = messages
      .filter((item) => item.role === 'user' || item.role === 'assistant')
      .slice(-10)
      .map(({ role, content }) => ({ role, content }))

    setMessages((current) => [...current, { role: 'user', content: message }])
    setInput('')
    setError('')
    setSending(true)

    try {
      const response = await api.post('/assistant/chat', { message, history })
      setMessages((current) => [
        ...current,
        { role: 'assistant', content: response.data.data.reply },
      ])
    } catch (requestError) {
      setError(extractError(requestError))
    } finally {
      setSending(false)
    }
  }

  function handleSubmit(event) {
    event.preventDefault()
    sendMessage()
  }

  return (
    <Layout title="Sage AI · Shopping concierge">
      <section className="assistant-hero">
        <div className="assistant-hero-copy">
          <span className="assistant-eyebrow"><span className="assistant-pulse" /> YOUR PERSONAL SHOPPING CONCIERGE</span>
          <h1>Good finds.<br /><em>Better decisions.</em></h1>
          <p>Tell Sage what you need. Get thoughtful recommendations picked from the products in our store.</p>
          <div className="assistant-trust"><span>✦</span> Knows what’s in stock <span>·</span> Prices in rupees <span>·</span> No guesswork</div>
        </div>
        <div className="assistant-orbit" aria-hidden="true">
          <span className="orbit orbit-one" />
          <span className="orbit orbit-two" />
          <span className="orbit orbit-three" />
          <span className="orbit-sparkle">✳</span>
          <span className="orbit-dot orbit-dot-one" />
          <span className="orbit-dot orbit-dot-two" />
        </div>
      </section>

      <section className="assistant-workspace" aria-label="Chat with Sage">
        <div className="assistant-chat">
          <div className="assistant-chat-top">
            <div className="assistant-identity">
              <span className="assistant-avatar">✳</span>
              <span><strong>Sage</strong><small>Your SalesSavvy AI concierge</small></span>
            </div>
            <span className="assistant-online"><i /> READY TO HELP</span>
          </div>

          <div className="assistant-messages" aria-live="polite">
            {messages.map((message, index) => (
              <div className={`assistant-message assistant-message-${message.role}`} key={`${message.role}-${index}`}>
                {message.role === 'assistant' && <span className="assistant-message-avatar">✳</span>}
                <div className="assistant-bubble">
                  {message.content.split('\n').map((line, lineIndex) => (
                    <span className="assistant-message-line" key={lineIndex}>{line || '\u00a0'}</span>
                  ))}
                </div>
              </div>
            ))}
            {sending && (
              <div className="assistant-message assistant-message-assistant">
                <span className="assistant-message-avatar">✳</span>
                <div className="assistant-bubble assistant-typing" aria-label="Sage is thinking">
                  <i /><i /><i />
                </div>
              </div>
            )}
            <div ref={conversationEnd} />
          </div>

          {messages.length === 1 && (
            <div className="assistant-suggestions">
              {suggestions.map((suggestion) => (
                <button key={suggestion} type="button" onClick={() => sendMessage(suggestion)}>
                  {suggestion}<span>↗</span>
                </button>
              ))}
            </div>
          )}

          <ErrorAlert>{error}</ErrorAlert>
          <form className="assistant-composer" onSubmit={handleSubmit}>
            <textarea
              value={input}
              onChange={(event) => setInput(event.target.value)}
              placeholder="I’m looking for..."
              aria-label="Message Sage"
              maxLength={1000}
              rows={1}
            />
            <button className="assistant-send" type="submit" disabled={sending || !input.trim()} aria-label="Send message">
              {sending ? '…' : '↑'}
            </button>
          </form>
          <p className="assistant-disclaimer">Sage can make mistakes. Your messages and relevant product details are sent to the configured AI provider.</p>
        </div>
        <aside className="assistant-aside">
          <div className="assistant-aside-label">A LITTLE MORE YOU</div>
          <h2>Shopping should feel personal.</h2>
          <p>Start with a person, a moment, or a little “I just need something that…”</p>
          <div className="assistant-prompt-list">
            <span>“A desk refresh that feels calm”</span>
            <span>“A useful gift for a new student”</span>
            <span>“Something small to make home cozier”</span>
          </div>
          <div className="assistant-note"><span>✦</span><p>Sage only recommends items from the live SalesSavvy catalog and won’t invent prices or availability.</p></div>
        </aside>
      </section>
    </Layout>
  )
}

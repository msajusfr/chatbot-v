import ReactMarkdown from 'react-markdown';

export function MarkdownBlock({ content }: { content: string }) {
  return <ReactMarkdown>{content}</ReactMarkdown>;
}

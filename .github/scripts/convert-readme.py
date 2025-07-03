#!/usr/bin/env python3
"""
Convert README.md to HTML for GitHub Pages deployment.
"""

import re
import sys
import os


def convert_markdown_to_html(markdown_content):
    """Convert markdown content to HTML."""
    html = markdown_content

    # Convert headers
    html = re.sub(r'^### (.*?)$', r'<h3>\1</h3>', html, flags=re.MULTILINE)
    html = re.sub(r'^## (.*?)$', r'<h2>\1</h2>', html, flags=re.MULTILINE)
    html = re.sub(r'^# (.*?)$', r'<h1>\1</h1>', html, flags=re.MULTILINE)

    # Convert badges (keep as is, they're already HTML-compatible)
    # Badges are in the format [![...](...))](...)

    # Convert code blocks
    html = re.sub(r'```(\w+)?\n(.*?)```', r'<pre><code class="language-\1">\2</code></pre>', html, flags=re.DOTALL)
    html = re.sub(r'`([^`\n]+)`', r'<code>\1</code>', html)

    # Convert links
    html = re.sub(r'\[([^\]]+)\]\(([^)]+)\)', r'<a href="\2">\1</a>', html)

    # Convert bold text
    html = re.sub(r'\*\*(.*?)\*\*', r'<strong>\1</strong>', html)

    # Convert italic text
    html = re.sub(r'\*(.*?)\*', r'<em>\1</em>', html)

    # Convert unordered lists
    lines = html.split('\n')
    in_list = False
    result_lines = []

    for line in lines:
        if re.match(r'^- ', line):
            if not in_list:
                result_lines.append('<ul>')
                in_list = True
            list_item = re.sub(r'^- (.*)', r'<li>\1</li>', line)
            result_lines.append(list_item)
        else:
            if in_list:
                result_lines.append('</ul>')
                in_list = False
            result_lines.append(line)

    if in_list:
        result_lines.append('</ul>')

    html = '\n'.join(result_lines)

    # Convert paragraphs (split by double newlines)
    paragraphs = html.split('\n\n')
    html_paragraphs = []

    for para in paragraphs:
        para = para.strip()
        if para:
            # Don't wrap headers, lists, code blocks, or existing HTML in paragraphs
            if not (para.startswith('<h') or para.startswith('<ul') or
                   para.startswith('<pre') or para.startswith('<div') or
                   para.startswith('<li') or para.startswith('[![')):
                para = f'<p>{para}</p>'
            html_paragraphs.append(para)

    html = '\n\n'.join(html_paragraphs)

    # Clean up extra whitespace
    html = re.sub(r'\n{3,}', '\n\n', html)

    return html


def main():
    if len(sys.argv) != 4:
        print("Usage: python convert-readme.py <readme_file> <template_file> <output_file>")
        sys.exit(1)

    readme_file = sys.argv[1]
    template_file = sys.argv[2]
    output_file = sys.argv[3]

    # Read the README content
    try:
        with open(readme_file, 'r', encoding='utf-8') as f:
            readme_content = f.read()
    except FileNotFoundError:
        print(f"Error: README file '{readme_file}' not found")
        sys.exit(1)

    # Read the template
    try:
        with open(template_file, 'r', encoding='utf-8') as f:
            template_content = f.read()
    except FileNotFoundError:
        print(f"Error: Template file '{template_file}' not found")
        sys.exit(1)

    # Convert README to HTML
    readme_html = convert_markdown_to_html(readme_content)

    # Get version from environment variable if available
    version = os.getenv('RELEASE_VERSION', 'Latest')

    # Replace placeholders in template
    final_html = template_content.replace('{{CONTENT}}', readme_html)
    final_html = final_html.replace('{{VERSION}}', version)
    final_html = final_html.replace('{{DATE}}', os.popen('date +"%Y-%m-%d"').read().strip())

    # Write the output
    os.makedirs(os.path.dirname(output_file), exist_ok=True)
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(final_html)

    print(f"Successfully converted {readme_file} to {output_file}")


if __name__ == '__main__':
    main()

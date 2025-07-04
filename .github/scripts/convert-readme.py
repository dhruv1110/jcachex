#!/usr/bin/env python3
"""
Convert README.md to HTML for GitHub Pages deployment.
"""

import re
import sys
import os
import html


def convert_markdown_to_html(markdown_content):
    """Convert markdown content to HTML with improved parsing."""
    html = markdown_content

    # First, protect code blocks by replacing them with placeholders
    code_blocks = []
    def replace_code_block(match):
        code_blocks.append(match.group(0))
        return f"__CODE_BLOCK_{len(code_blocks)-1}__"

    html = re.sub(r'```(\w+)?\n(.*?)```', replace_code_block, html, flags=re.DOTALL)

    # Convert inline code (protect these too)
    inline_code = []
    def replace_inline_code(match):
        inline_code.append(match.group(0))
        return f"__INLINE_CODE_{len(inline_code)-1}__"

    html = re.sub(r'`([^`\n]+)`', replace_inline_code, html)

    # Convert headers (in order from most specific to least specific)
    html = re.sub(r'^#### (.*?)$', r'<h4>\1</h4>', html, flags=re.MULTILINE)
    html = re.sub(r'^### (.*?)$', r'<h3>\1</h3>', html, flags=re.MULTILINE)
    html = re.sub(r'^## (.*?)$', r'<h2>\1</h2>', html, flags=re.MULTILINE)
    html = re.sub(r'^# (.*?)$', r'<h1>\1</h1>', html, flags=re.MULTILINE)

    # Convert links (including badges)
    html = re.sub(r'\[([^\]]+)\]\(([^)]+)\)', r'<a href="\2">\1</a>', html)

    # Convert bold and italic text
    html = re.sub(r'\*\*(.*?)\*\*', r'<strong>\1</strong>', html)
    html = re.sub(r'\*(.*?)\*', r'<em>\1</em>', html)

    # Convert nested lists and simple lists
    lines = html.split('\n')
    in_list = False
    result_lines = []

    i = 0
    while i < len(lines):
        line = lines[i]

        # Handle nested lists (with indentation)
        if re.match(r'^\s*- ', line):
            if not in_list:
                result_lines.append('<ul>')
                in_list = True
            # Handle nested items
            indent_level = len(line) - len(line.lstrip())
            if indent_level > 0:
                # This is a nested item
                nested_item = re.sub(r'^\s*- (.*)', r'<li>\1</li>', line)
                result_lines.append(f'  {nested_item}')
            else:
                # This is a top-level item
                list_item = re.sub(r'^- (.*)', r'<li>\1</li>', line)
                result_lines.append(list_item)
        else:
            if in_list:
                result_lines.append('</ul>')
                in_list = False
            result_lines.append(line)

        i += 1

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
                   para.startswith('<li') or para.startswith('[![') or
                   para.startswith('<p>') or para.startswith('<a') or
                   '__CODE_BLOCK_' in para or '__INLINE_CODE_' in para):
                para = f'<p>{para}</p>'
            html_paragraphs.append(para)

    html = '\n\n'.join(html_paragraphs)

    # Now restore the code blocks
    for i, code_block in enumerate(code_blocks):
        # Parse the code block properly
        match = re.match(r'```(\w+)?\n(.*?)```', code_block, flags=re.DOTALL)
        if match:
            lang = match.group(1) or ''
            code = match.group(2)

            # HTML escape the code content to prevent XML/HTML interpretation
            escaped_code = html.escape(code)

            # Add proper language class and structure for syntax highlighting
            if lang.lower() == 'xml':
                formatted_code = f'<pre class="language-xml"><code class="language-xml">{escaped_code}</code></pre>'
            elif lang.lower() in ['kotlin', 'java', 'groovy']:
                formatted_code = f'<pre class="language-{lang.lower()}"><code class="language-{lang.lower()}">{escaped_code}</code></pre>'
            else:
                formatted_code = f'<pre><code class="language-{lang}">{escaped_code}</code></pre>'

            html = html.replace(f'__CODE_BLOCK_{i}__', formatted_code)
        else:
            html = html.replace(f'__CODE_BLOCK_{i}__', code_block)

    # Restore inline code
    for i, inline in enumerate(inline_code):
        # Parse inline code properly
        match = re.match(r'`([^`\n]+)`', inline)
        if match:
            code = match.group(1)
            # HTML escape inline code content too
            escaped_code = html.escape(code)
            formatted_code = f'<code>{escaped_code}</code>'
            html = html.replace(f'__INLINE_CODE_{i}__', formatted_code)
        else:
            html = html.replace(f'__INLINE_CODE_{i}__', inline)

    # Clean up extra whitespace and improve formatting
    html = re.sub(r'\n{3,}', '\n\n', html)
    html = re.sub(r'<p>\s*</p>', '', html)  # Remove empty paragraphs
    html = re.sub(r'<li>\s*</li>', '', html)  # Remove empty list items

    return html


def replace_version_placeholders(content, version):
    """Replace x.y.z placeholders with actual version number."""
    if not version or version == 'Latest':
        return content

    # Replace x.y.z in dependency examples
    content = re.sub(r'x\.y\.z', version, content)

    # Also replace any other version placeholders
    content = re.sub(r'\{version\}', version, content)
    content = re.sub(r'\$\{version\}', version, content)

    return content


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

    # Get version from environment variable
    version = os.getenv('RELEASE_VERSION', '0.1.11')  # Default to current version

    # Replace version placeholders in README content
    readme_content = replace_version_placeholders(readme_content, version)

    # Convert README to HTML
    readme_html = convert_markdown_to_html(readme_content)

    # Replace placeholders in template
    final_html = template_content.replace('{{CONTENT}}', readme_html)
    final_html = final_html.replace('{{VERSION}}', version)
    final_html = final_html.replace('{{DATE}}', os.popen('date +"%Y-%m-%d"').read().strip())

    # Write the output
    os.makedirs(os.path.dirname(output_file), exist_ok=True)
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(final_html)

    print(f"Successfully converted {readme_file} to {output_file}")
    print(f"Version: {version}")


if __name__ == '__main__':
    main()

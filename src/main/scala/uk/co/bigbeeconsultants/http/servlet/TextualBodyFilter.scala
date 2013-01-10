//-----------------------------------------------------------------------------
// The MIT License
//
// Copyright (c) 2012 Rick Beton <rick@bigbeeconsultants.co.uk>
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//-----------------------------------------------------------------------------

package uk.co.bigbeeconsultants.http.servlet

import uk.co.bigbeeconsultants.http._

/**
 * Provides hooks to modify a textual body in transit. This can be applied both to request bodies and response bodies.
 * @param lineProcessor the mutation function that is applied to every line of the body content.
 *                When applied to a body, this is used only when the content is treated as text (see `processAsText`).
 * @param processAsText an optional condition that limits when the rewrite function will be used. By default,
 *                      the response body is simply copied verbatim as binary data; therefore the rewrite
 *                      function is ignored. A suggested alternative value is `AllTextualMediaTypes`. Note that there
 *                      are no possible values that will force the conversion of binary content to text.
 */
case class TextualBodyFilter(lineProcessor: TextFilter = NoChangeTextFilter,
                             processAsText: MediaFilter = NoMediaTypes)

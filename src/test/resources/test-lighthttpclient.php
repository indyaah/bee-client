<?php
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
//print_r($_REQUEST);
//print_r($_COOKIE);

function getInterestingServerVars() {
    $svrKeys = array();
    foreach ($_SERVER as $key=>$val) {
        if (strpos($key, "REQUEST_") === 0 ||
            strpos($key, "CONTENT_") === 0 ||
            strpos($key, "QUERY_") === 0 ||
            strpos($key, "SERVER_") === 0 ||
            strpos($key, "HTTP_") === 0) {
            array_push($svrKeys, $key);
        }
    }
    sort($svrKeys);
    return $svrKeys;
}

function dumpInterestingServerVars($assign, $separator) {
    $s = '';
    foreach (getInterestingServerVars() as $key) {
        $val = $_SERVER[$key];
        $s .= "$key$assign$val$separator";
    }
    return $s;
}

$method = strip_tags($_SERVER['REQUEST_METHOD']);
if ($method == 'GET' || $method == 'HEAD' || isset($_REQUEST['D']))
{
    $ct = strip_tags($_REQUEST['CT']);
    if (isset($ct) && $ct == 'text/plain')
    {
        header("Content-Type: text/plain");
        echo dumpInterestingServerVars(': ', "\n");
    }
    else
    {
?><html><head>
<title>Test Script for LightHttpClient</title>
</head>
<body>
<img src="B.png" width="16" height="16" alt="B symbol"/>
<pre><![CDATA[
<?php echo dumpInterestingServerVars(': ', "\n") ?>
<?php //print_r($_SERVER); ?>
]]></pre>
<ul>
<li><a href="?CT=text/plain">text/plain</a></li>
</ul>
</body>
</html>
<?php
    }
}
else
{
    $qs = '?';
    foreach ($_REQUEST as $key=>$val)
    {
        $qs .= urlencode($key) . '=' . urlencode($val) . '&';
    }
    if (isset($_SERVER['HTTP_HOST']))
    {
        $host = $_SERVER['HTTP_HOST'];
    }
    else
    {
        $host = $_SERVER['SERVER_ADDR'];
    }
    header('Location: http://' . $host . $_SERVER['PHP_SELF'] . $qs);
}
?>
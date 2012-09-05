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
    foreach ($_GET as $key => $val) {
        $s .= "GET: $key$assign$val$separator";
    }
    foreach ($_POST as $key => $val) {
        $s .= "POST: $key$assign$val$separator";
    }
    /* PUT data comes in on the stdin stream */
    $putdata = fopen("php://input", "r");
    if ($putdata !== FALSE) {
        $s .= "PUT: ";
        while ($data = fread($putdata, 1024)) $s .= $data;
        fclose($putdata);
    }
    return $s;
}

$method = strip_tags($_SERVER['REQUEST_METHOD']);
if ($method == 'GET' || $method == 'HEAD' || isset($_REQUEST['D']))
{
    if (isset($_REQUEST['CT']) && strip_tags($_REQUEST['CT']) == 'text/plain')
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
<pre>
<?php if (!isset($_REQUEST['STUM'])) { echo dumpInterestingServerVars(': ', "\n"); } ?>
</pre>
<ul>
<li><a href="?CT=text/plain">text/plain</a></li>
</ul>
<?php if (isset($_REQUEST['LOREM'])) {
      for ($i = 1; $i <= 1000; $i++) { ?>
<p><?php echo $i ?>
 Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore
magna aliquam erat volutpat. Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis
nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie
consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit
praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Nam liber tempor cum soluta nobis eleifend
option congue nihil imperdiet doming id quod mazim placerat facer possim assum. Typi non habent claritatem insitam;
est usus legentis in iis qui facit eorum claritatem. Investigationes demonstraverunt lectores legere me lius quod ii
legunt saepius. Claritas est etiam processus dynamicus, qui sequitur mutationem consuetudium lectorum. Mirum est notare
quam littera gothica, quam nunc putamus parum claram, anteposuerit litterarum formas humanitatis per seacula quarta
decima et quinta decima. Eodem modo typi, qui nunc nobis videntur parum clari, fiant sollemnes in futurum.
</p>
<?php } } ?>
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
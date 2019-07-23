<%--
  ============LICENSE_START==========================================
  ONAP Portal SDK
  ===================================================================
  Copyright Â© 2017 AT&T Intellectual Property. All rights reserved.
  ===================================================================

  Unless otherwise specified, all software contained herein is licensed
  under the Apache License, Version 2.0 (the â€œLicenseâ€);
  you may not use this software except in compliance with the License.
  You may obtain a copy of the License at

              http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  Unless otherwise specified, all documentation contained herein is licensed
  under the Creative Commons License, Attribution 4.0 Intl. (the â€œLicenseâ€);
  you may not use this documentation except in compliance with the License.
  You may obtain a copy of the License at

              https://creativecommons.org/licenses/by/4.0/

  Unless required by applicable law or agreed to in writing, documentation
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.

  ============LICENSE_END============================================


  --%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="org.onap.portalsdk.core.util.SystemProperties"%>

<%-- Redirected because we can't set the welcome page to a virtual URL. --%>
<%-- Forward to the intended start page to reduce frustration for new users. --%>

<%
        String scheme = request.getScheme() + "://";
        String servername = request.getServerName();
        Integer urlPort = request.getServerPort();
        String contextPath = request.getContextPath();
        String htmFile = "/login.htm";
        request.getSession().invalidate();
        String redirectURL = "";
        redirectURL = redirectURL + scheme + servername;
        if ((urlPort != null) && (urlPort.intValue() != 80) && (urlPort.intValue() != 443)
                        && (urlPort.intValue() != -1)) {
                redirectURL += ":" + urlPort;
        }
        redirectURL += contextPath + htmFile;
%>
<c:redirect url="<%=redirectURL%>"></c:redirect>
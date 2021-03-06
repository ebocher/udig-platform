/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2011, Jesse Eichar
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package eu.udig.catalog.csw

import java.io.InputStream
import java.net.URL
import java.util.logging.Logger

trait Request[R] {
  def inProcessor: InputStream => R
  def cswRequestName: String
  def params: Seq[(String, Any)] =
    Seq("request" -> cswRequestName,
      "version" -> "2.0.2",
      "service" -> "CSW")

  def createUrlString(context: Context) = {
    val paramString = params.map { case (key, value) => key + "=" + value }.mkString("&")
    paramString match {
      case p if p.isEmpty => context.baseURL
      case p if context.baseURL contains '?' => context.baseURL + "&" + p
      case p => context.baseURL + "?" + p 
    }
  }
  def openConnection(context: Context) = {
    val urlString = createUrlString(context)
    Logging.info("Executing: " + urlString)

    val url = new URL(urlString)
    val conn = url.openConnection
    conn.setReadTimeout(context.timeout)
    conn.asInstanceOf[java.net.HttpURLConnection]
  }
  def execute(context: Context): R
}
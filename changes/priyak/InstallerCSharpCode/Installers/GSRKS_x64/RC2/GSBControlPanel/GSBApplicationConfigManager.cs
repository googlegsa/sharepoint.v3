//Copyright 2009 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.


using System;
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Xml.XPath;

namespace GSBControlPanel
{
    public class GSBApplicationConfigManager
    {
        public const string CONFIGURATION = "configuration";
        public const string APPSETTINGS = "appSettings";
        public const string ADD_ELEMENT = "add";
        public const string KEY_ATTRIBUTE = "key";
        public const string VALUE_ATTRIBUTE = "value";
        public const string TYPE_ATTRIBUTE = "type";
        public const string NAME_ATTRIBUTE = "name";
        public const string MODULES_SECTION = "//modules"; // This refers to the Modules section in web.config file for IIS 7

        private string myFileName = "";
        private XmlDocument xd = new XmlDocument();

        /// <summary>
        /// Loads the XML document
        /// </summary>
        /// <param name="path"></param>
        public void LoadXML(string path)
        {
            if ((path != null) && (!path.Trim().Equals("")))
            {
                myFileName = path;
                try
                {
                    xd.Load(path);
                }
                catch (Exception) { }
            }
        }
        /// <summary>
        /// Ensure that all the nodes are in place, else create them
        /// </summary>
        public void EnsureParentNodesForGSAParameters()
        {
            if (xd != null)
            {
                String pattern = "/";
                XmlNode rootNode = xd.SelectSingleNode(pattern);

                pattern += CONFIGURATION;
                XmlNode parentNode = xd.SelectSingleNode(pattern);
                if (null == parentNode)
                {
                    parentNode = rootNode.AppendChild(xd.CreateElement(CONFIGURATION));
                }

                XmlNode child = parentNode.SelectSingleNode(APPSETTINGS);
                if (null == child)
                {
                    parentNode = parentNode.AppendChild(xd.CreateElement(APPSETTINGS));
                }

                SaveXML();//save the xmlfile
                LoadXML(myFileName);//reload updated xml file
            }
        }

        /// <summary>
        /// deletes the node based on the pattern
        /// </summary>
        /// <param name="pattern"></param>
        public void DeleteNode(string pattern)
        {
            if ((xd != null) && (pattern != null))
            {
                try
                {
                    XmlNode node = xd.SelectSingleNode(pattern);
                    if (null != node)
                    {
                        XmlNode xx = node.ParentNode;
                        xx.RemoveChild(node);
                    }

                    //xd.SelectSingleNode(pattern).ParentNode.RemoveAll();
                }
                catch (Exception) { }
            }

        }
        /// <summary>
        /// Modify the Node value
        /// </summary>
        /// <param name="pattern"></param>
        /// <param name="value"></param>
        public void ModifyNode(string pattern, string attributename, string value)
        {
            string myPattern = pattern + "/add[@key='" + attributename + "']";
            //Logic: Find the node based on the input pattern (uses XPath Query internally)
            //If you dont find node cretae a new node

            if ((xd != null) && (pattern != null))
            {
                XmlNode node = xd.SelectSingleNode(myPattern);
                if (node != null)
                {
                    XmlElement ele = node as XmlElement;
                    if (null != ele)
                    {
                        ele.SetAttribute(VALUE_ATTRIBUTE, value);
                    }
                }
                else
                {
                    //create a new node with the given value
                    //check all the parent nodes are in place 

                    //Get the parent node
                    XmlNode ParentNode = xd.SelectSingleNode(pattern);
                    if (null != ParentNode)
                    {
                        XmlNode childNode = ParentNode.AppendChild(xd.CreateElement(ADD_ELEMENT));//create child node
                        XmlElement ele = childNode as XmlElement;
                        if (null != ele)
                        {
                            ele.SetAttribute(KEY_ATTRIBUTE, attributename);
                            ele.SetAttribute(VALUE_ATTRIBUTE, value);
                        }
                    }
                }
            }//end: if ((xd != null) && (pattern != null))

        }

        /// <summary>
        /// Modify node value for HTTP module
        /// </summary>
        /// <param name="pattern1">Refers to the 'httpModules' element in web.config file.</param>
        /// <param name="pattern2">Refers to the 'modules' element in web.config file.</param>
        /// <param name="attributename">Name of attribute to be added</param>
        /// <param name="type">Refers to the type attribute</param>
        public void ModifyNodeForHttpModule(string pattern1,string pattern2, string attributename, string type)
        {
            /*
             * Load up the modules element in web.config file. If the 'modules' element is found (this is true in case of SP 2010 
             * installation on 64-bit machine), add the session module underneath the modules element. This ensures that the 
             * session module gets run for all requests and not just for known ASP.NET resources.
             */
            XmlNode xmlNode = xd.SelectSingleNode(pattern2);

            if (xmlNode == null)
            {
                /*
                 * Load up the httpmodules element in web.config file. If the 'modules' element is not found (this is true in case of SP 2007 
                 * installation on 64-bit machine), add the session module underneath the httpmodules element. 
                 */
                xmlNode = xd.SelectSingleNode(pattern1);
            }
            if (xmlNode != null)
            {
                XmlElement element = xmlNode as XmlElement;
                /*
                 * Attempt to load up the entry for httpModule(MOSS 2007 on 64-bit) or modules(SharePoint 2010 on 64-bit). 
                 * Either one of them will surely exist.
                 */
                element = (XmlElement)xmlNode.SelectSingleNode(String.Format("//add[@name='{0}']", attributename));

                // Check if it exists...
                if (element != null)
                {
                    /*
                     * Name was found, change value for 'type' attribute here (only needed if you want to change it).
                     * For Instance, the name <add name="Session" /> may be found in web.config file, without any type 
                     * attribute. Hence, the code adds the type attribute (i.e. System.Web.SessionState.SessionStateModule)
                     * to the httpmodule/modules entry.
					 */
                    element.SetAttribute(TYPE_ATTRIBUTE, type);
                }
                else 
                {
                    // Name was not found, so create the 'add' element and set it's name/type attributes.

                    element = xd.CreateElement(ADD_ELEMENT);
                    element.SetAttribute(NAME_ATTRIBUTE, attributename);
                    element.SetAttribute(TYPE_ATTRIBUTE, type);
                    xmlNode.AppendChild(element);
                }
            }
        }

        /// <summary>
        /// Get the Value of the node. E.g. for node "<add key="accessLevel" value="a" />" value is "a"
        /// </summary>
        /// <param name="pattern"></param>
        public string GetNodeValue(string pattern)
        {
            if ((xd != null) && (pattern != null))
            {
                try
                {
                    XmlNode node = xd.SelectSingleNode(pattern);
                    if ((node != null) && (node.Attributes != null))
                    {
                        return node.Attributes["value"].Value;
                    }
                }
                catch (Exception) { }
            }
            return null;
        }

        /// <summary>
        /// Save the modified XML document
        /// </summary>
        public void SaveXML()
        {
            //Logic: Why I have kept save separate? because you can do multiple operations in memory and call save just once so less disk operations 
            if ((null != xd) && (myFileName != null) && (!myFileName.Trim().Equals("")))
            {
                xd.Save(myFileName);
            }
        }
        
    }
}

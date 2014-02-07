/*******************************************************************************
 * Copyright (c) 2011, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.orion.server.git.servlets;

import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jgit.transport.TransportHttp;
import org.eclipse.orion.internal.server.servlets.ServletResourceHandler;
import org.eclipse.orion.server.core.PreferenceHelper;
import org.eclipse.orion.server.core.ServerStatus;
import org.eclipse.orion.server.git.objects.*;
import org.eclipse.orion.server.git.objects.Status;

/**
 * A git handler for Orion Git API v 1.0.
 */
public class GitHandlerV1 extends ServletResourceHandler<String> {

	private ServletResourceHandler<String> branchHandlerV1;
	private ServletResourceHandler<String> cloneHandlerV1;
	private ServletResourceHandler<String> commitHandlerV1;
	private ServletResourceHandler<String> configHandlerV1;
	private ServletResourceHandler<String> diffHandlerV1;
	private ServletResourceHandler<String> indexHandlerV1;
	private ServletResourceHandler<String> remoteHandlerV1;
	private ServletResourceHandler<String> statusHandlerV1;
	private ServletResourceHandler<String> tagHandlerV1;
	private ServletResourceHandler<String> blameHandlerV1;

	private ServletResourceHandler<IStatus> statusHandler;

	//Used for enabling SSO git operations over http
	private static java.lang.reflect.Method SSO_METHOD;
	private static boolean SSO_METHOD_INITIALIZED;

	GitHandlerV1(ServletResourceHandler<IStatus> statusHandler) {
		branchHandlerV1 = new GitBranchHandlerV1(statusHandler);
		blameHandlerV1 = new GitBlameHandlerV1(statusHandler);
		cloneHandlerV1 = new GitCloneHandlerV1(statusHandler);
		commitHandlerV1 = new GitCommitHandlerV1(statusHandler);
		configHandlerV1 = new GitConfigHandlerV1(statusHandler);
		diffHandlerV1 = new GitDiffHandlerV1(statusHandler);
		indexHandlerV1 = new GitIndexHandlerV1(statusHandler);
		remoteHandlerV1 = new GitRemoteHandlerV1(statusHandler);
		statusHandlerV1 = new GitStatusHandlerV1(statusHandler);
		tagHandlerV1 = new GitTagHandlerV1(statusHandler);
		this.statusHandler = statusHandler;
	}

	@Override
	public boolean handleRequest(HttpServletRequest request, HttpServletResponse response, String gitPathInfo) throws ServletException {

		String[] infoParts = gitPathInfo.split("\\/", 3); //$NON-NLS-1$

		String pathString = infoParts[2];
		if (request.getContextPath().length() != 0) {
			IPath path = pathString == null ? Path.EMPTY : new Path(pathString);
			IPath contextPath = new Path(request.getContextPath());
			if (contextPath.isPrefixOf(path)) {
				pathString = path.removeFirstSegments(contextPath.segmentCount()).toString();
			}
		}

		//TODO: Add to constants
		System.out.println("==============================================================");
		String tokenName = PreferenceHelper.getString("lpta.token.name"); //$NON-NLS-1$
		System.out.println("TokenName " + tokenName);
		if (tokenName != null) {
			javax.servlet.http.Cookie[] cookies = request.getCookies();
			System.out.println("Cookies length " + cookies.length);
			if (cookies != null) {
				for (int i = 0; i < cookies.length; i++) {
					Cookie currentCookie = cookies[i];
					System.out.println("Current cookie " + currentCookie.getName());
					if (tokenName.equals(currentCookie.getName())) {
						System.out.println("TOKEN cookie " + currentCookie.getName() + " " + currentCookie.getValue());
						Cookie loginCookie = new Cookie(currentCookie.getName(), currentCookie.getValue());
						if (SSO_METHOD == null && !SSO_METHOD_INITIALIZED) {
							try {
								SSO_METHOD_INITIALIZED = true;
								SSO_METHOD = TransportHttp.class.getMethod("setSSOToken", Cookie.class);
								System.out.println("Got TransportMethod");
							} catch (RuntimeException e) {
								return statusHandler.handleRequest(request, response, new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "RuntimeException while trying to look up JGit call", e));
							} catch (NoSuchMethodException e) {
								return statusHandler.handleRequest(request, response, new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "NoSuchMethodException while trying to look up JGit call", e));
							}
						}
						if (SSO_METHOD != null) {
							try {
								System.out.println("Setting Cookie " + Thread.currentThread().getId() + " Request " + request.getRequestURL());
								SSO_METHOD.invoke(null, loginCookie);
							} catch (IllegalArgumentException e) {
								return statusHandler.handleRequest(request, response, new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "IllegalArgumentException while trying to set token", e));
							} catch (IllegalAccessException e) {
								return statusHandler.handleRequest(request, response, new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "IllegalAccessException while trying to set token", e));
							} catch (InvocationTargetException e) {
								return statusHandler.handleRequest(request, response, new ServerStatus(IStatus.ERROR, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "InvocationTargetException while trying to set token", e));
							}
						}
					}
				}
			}
		}
		System.out.println("==============================================================");

		if (infoParts[1].equals(Branch.RESOURCE)) {
			return branchHandlerV1.handleRequest(request, response, pathString);
		} else if (infoParts[1].equals(Clone.RESOURCE)) {
			return cloneHandlerV1.handleRequest(request, response, pathString);
		} else if (infoParts[1].equals(Commit.RESOURCE)) {
			return commitHandlerV1.handleRequest(request, response, pathString);
		} else if (infoParts[1].equals(ConfigOption.RESOURCE)) {
			return configHandlerV1.handleRequest(request, response, pathString);
		} else if (infoParts[1].equals(Diff.RESOURCE)) {
			return diffHandlerV1.handleRequest(request, response, pathString);
		} else if (infoParts[1].equals(Index.RESOURCE)) {
			return indexHandlerV1.handleRequest(request, response, pathString);
		} else if (infoParts[1].equals(Remote.RESOURCE)) {
			return remoteHandlerV1.handleRequest(request, response, pathString);
		} else if (infoParts[1].equals(Status.RESOURCE)) {
			return statusHandlerV1.handleRequest(request, response, pathString);
		} else if (infoParts[1].equals(Tag.RESOURCE)) {
			return tagHandlerV1.handleRequest(request, response, pathString);
		} else if (infoParts[1].equals(Blame.RESOURCE)) {
			return blameHandlerV1.handleRequest(request, response, pathString);
		}
		return false;
	}
}

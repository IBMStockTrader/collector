/*
       Copyright 2021 IBM Corp All Rights Reserved

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ibm.hybrid.cloud.sample.stocktrader.collector;

import com.cloudant.client.api.Database;

import com.ibm.hybrid.cloud.sample.stocktrader.collector.json.Evidence;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//Logging (JSR 47)
import java.util.logging.Level;
import java.util.logging.Logger;

//CDI 2.0
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.enterprise.context.ApplicationScoped;

//mpConfig 1.3
import org.eclipse.microprofile.config.inject.ConfigProperty;

//mpJWT 1.1
//import org.eclipse.microprofile.auth.LoginConfig;

//mpMetrics 2.0
import org.eclipse.microprofile.metrics.annotation.Counted;

//Servlet 4.0
import javax.servlet.http.HttpServletRequest;

//JAX-RS 2.1 (JSR 339)
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

@ApplicationPath("/")
@Path("/")
//@LoginConfig(authMethod = "MP-JWT", realmName = "jwt-jaspi")
@ApplicationScoped //enable interceptors (note you need a WEB-INF/beans.xml in your war)
/** This microservice can be fed evidence about security and compliance related events.
 *  It keeps track of such evidence and responds to periodic "scrapes" of its endpoint
 *  from the IBM Security and Compliance Center (SCC).  This version persists the
 *  evidence to an IBM Cloudant non-SQL datastore, so that nothing is lost if the pod
 *  is scaled down and later is scaled back up. 
 */
public class CollectorService extends Application {
	private static Logger logger = Logger.getLogger(CollectorService.class.getName());

	private static final int CONFLICT = 409;         //odd that JAX-RS has no ConflictException

	private static SimpleDateFormat formatter = null;

	@Resource(lookup="cloudant/CollectorDB") Database collectorDB;  //defined in the server.xml


	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
//	@RolesAllowed({"StockTrader", "StockViewer"}) //Couldn't get this to work; had to do it through the web.xml instead :(
	public Evidence[] getAllEvidence() throws IOException {
		List<Evidence> evidenceList = collectorDB.getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(Evidence.class);
		int size = evidenceList.size();
		Evidence[] evidenceArray = new Evidence[size];
		evidenceArray = evidenceList.toArray(evidenceArray);

		System.out.println("Returning "+size+" evidence entries");
		for (int index=0; index<size; index++) {
			Evidence evidence = evidenceArray[index];
			System.out.println("evidence["+index+"]="+evidence);
		}
		return evidenceArray;
	}

        @POST
        @Path("/")
        @Produces(MediaType.APPLICATION_JSON)
//      @RolesAllowed({"StockTrader", "StockViewer"}) //Couldn't get this to work; had to do it through the web.xml instead :(
        public Evidence createEvidenceWithoutTimestamp(Evidence evidence) throws IOException {
		if (formatter==null) formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		Date now = new Date();
		String timestamp = formatter.format(now);
		evidence.setDate(timestamp);
		Evidence newEvidence = createEvidence(timestamp, evidence);
		return newEvidence;
	}

	@POST
	@Path("/{timestamp}")
	@Produces(MediaType.APPLICATION_JSON)
	@Counted(name="collectors", displayName="Stock Trader collectors", description="Number of evidence entries created in the Stock Trader application")
	//	@RolesAllowed({"StockTrader"}) //Couldn't get this to work; had to do it through the web.xml instead :(
	public Evidence createEvidence(@PathParam("timestamp") String timestamp, Evidence evidence) {
		if (timestamp != null) {
			boolean alreadyExists = collectorDB.contains(timestamp);

			if (!alreadyExists) {
				logger.info("Creating evidence entry for "+timestamp);

				collectorDB.save(evidence);
			} else {
				logger.warning("Evidence entry already exists for: "+timestamp);
				throw new WebApplicationException("Evidence entry already exists for "+timestamp+"!", CONFLICT);
			}

			logger.info("Evidence entry created successfully");
		}

		return evidence;
	}

	@GET
	@Path("/{timestamp}")
	@Produces(MediaType.APPLICATION_JSON)
//	@RolesAllowed({"StockTrader", "StockViewer"}) //Couldn't get this to work; had to do it through the web.xml instead :(
	public Evidence getEvidence(@PathParam("timestamp") String timestamp, @Context HttpServletRequest request) throws IOException {
		Evidence evidence = collectorDB.find(Evidence.class, timestamp);
		if (evidence != null) {
			logger.fine("Returning "+evidence.toString());
		} else {
			logger.warning("No evidence found for "+timestamp+" - throwing a 404");
			throw new NotFoundException(timestamp);
		}

		return evidence;
	}

	@PUT
	@Path("/{timestamp}")
	@Produces(MediaType.APPLICATION_JSON)
//	@RolesAllowed({"StockTrader"}) //Couldn't get this to work; had to do it through the web.xml instead :(
	public Evidence updateEvidence(@PathParam("timestamp") String timestamp, Evidence evidence, @Context HttpServletRequest request) throws IOException {
		Evidence oldEvidence = collectorDB.find(Evidence.class, timestamp);

		if (oldEvidence!=null) {
			evidence.set_rev(oldEvidence.get_rev()); //the update will be rejected with a 409 if the previous _rev value isn't passed

			logger.fine("Updating evidence into Cloudant: "+evidence);
			collectorDB.update(evidence);
		} else {
                        logger.warning("No evidence found for "+timestamp+" - throwing a 404");
			throw new NotFoundException(timestamp);
		}

		return evidence;
	}

	@DELETE
	@Path("/{timestamp}")
	@Produces(MediaType.APPLICATION_JSON)
//	@RolesAllowed({"StockTrader"}) //Couldn't get this to work; had to do it through the web.xml instead :(
	public Evidence deleteEvidence(@PathParam("timestamp") String timestamp) {
		Evidence evidence = collectorDB.find(Evidence.class, timestamp);

		if (evidence != null) {
			collectorDB.remove(evidence); //TODO: is destroying evidence really a good idea...

			logger.info("Successfully deleted evidence for "+timestamp); //exception would have occured otherwise
		} else {
                        logger.warning("No evidence found for "+timestamp+" - throwing a 404");
			throw new NotFoundException(timestamp);
		}

		return evidence; //maybe this method should return void instead?
	}
}

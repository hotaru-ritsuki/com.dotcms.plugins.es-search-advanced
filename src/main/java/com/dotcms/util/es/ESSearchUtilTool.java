package com.dotcms.util.es;

import com.dotcms.content.elasticsearch.business.ESSearchResults;
import com.dotcms.rendering.velocity.viewtools.content.ContentMap;
import com.dotmarketing.beans.Host;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.web.WebAPILocator;
import com.dotmarketing.common.model.ContentletSearch;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.exception.DotSecurityException;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.contentlet.model.Contentlet;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.PageMode;
import com.liferay.portal.model.User;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.context.ViewContext;
import org.apache.velocity.tools.view.tools.ViewTool;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ESSearchUtilTool implements ViewTool {
    private User user = null;
    private Context context;
    private Host currentHost;
    private PageMode mode;

    @Override
    public void init(Object initData) {
        this.context = ((ViewContext) initData).getVelocityContext();
        HttpServletRequest req = ((ViewContext) initData).getRequest();

        mode = PageMode.get(req);
        user = getUser(req);

        try{
            this.currentHost = WebAPILocator.getHostWebAPI().getCurrentHost(req);
        }catch(Exception e){
            Logger.error(this, "Error finding current host", e);
        }
    }

    public ESSearchResults search(String esQuery) throws DotSecurityException, DotDataException {
        ESSearchResults cons =  esSearch(esQuery, mode.showLive, user, true);
        List<ContentMap> maps = new ArrayList<>();


        for(Object x : cons){
            Contentlet con = (Contentlet)x;

            maps.add(new ContentMap(con, user, !mode.showLive, currentHost, context));
        }

        return new ESSearchResults(cons.getResponse(), maps);
    }

    public ESSearchResults esSearch(String esQuery, boolean live, User user, boolean respectFrontendRoles) throws DotSecurityException, DotDataException {
        SearchResponse resp = APILocator.getEsSearchAPI().esSearchRaw(esQuery, live, user, respectFrontendRoles);
        ESSearchResults contents = new ESSearchResults(resp, new ArrayList<>());
        contents.setQuery(esQuery);
        contents.setRewrittenQuery(esQuery);

        if (Objects.isNull(contents.getHits())) {
            return contents;
        }

        long start = System.currentTimeMillis();

        List<ContentletSearch> list = new ArrayList<>();
        for (SearchHit sh : contents.getHits()) {
            try {
                Map<String, Object> sourceMap = sh.getSourceAsMap();
                ContentletSearch conwrapper = new ContentletSearch();
                conwrapper.setInode(sourceMap.get("inode").toString());
                list.add(conwrapper);
            } catch (Exception e) {
                Logger.error(this, e.getMessage(), e);
            }
        }
        List<String> inodes = list.stream()
                .map(ContentletSearch::getInode)
                .collect(Collectors.toList());

        APILocator.getContentletAPIImpl().findContentlets(inodes).stream()
                .filter(contentlet -> Objects.nonNull(contentlet.getInode()))
                .forEach(contents::add);

        contents.setPopulationTook(System.currentTimeMillis() - start);
        return contents;
    }

}

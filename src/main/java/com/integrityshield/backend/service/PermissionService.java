package com.integrityshield.backend.service;

import com.integrityshield.backend.entity.AllowedApp;
import com.integrityshield.backend.entity.AllowedUrl;
import com.integrityshield.backend.repository.AllowedAppRepository;
import com.integrityshield.backend.repository.AllowedUrlRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final AllowedAppRepository appRepo;
    private final AllowedUrlRepository urlRepo;

    public PermissionService(AllowedAppRepository appRepo,
                             AllowedUrlRepository urlRepo) {
        this.appRepo = appRepo;
        this.urlRepo = urlRepo;
    }

    /* ================= APPS ================= */

    public void addAllowedApp(String appName) {

        appName = appName.replace("\"","").trim();

        if (appRepo.findByAppName(appName).isEmpty()) {
            appRepo.save(new AllowedApp(appName));
        }
    }

    public List<String> getAllowedApps() {

        return appRepo.findAll()
                .stream()
                .map(AllowedApp::getAppName)
                .collect(Collectors.toList());
    }

    public void removeAllowedApp(String appName) {

        appName = appName.replace("\"","").trim();
        appRepo.deleteByAppName(appName);
    }

    // 🔥 NEW: CLEAR ALL APPS
    public void clearAllApps() {
        appRepo.deleteAll();
    }

    /* ================= URLS ================= */

    public void addAllowedUrl(String url) {

        url = url.replace("\"","").trim();

        if (urlRepo.findByUrl(url).isEmpty()) {
            urlRepo.save(new AllowedUrl(url));
        }
    }

    public List<String> getAllowedUrls() {

        return urlRepo.findAll()
                .stream()
                .map(AllowedUrl::getUrl)
                .collect(Collectors.toList());
    }

    public void removeAllowedUrl(String url) {

        url = url.replace("\"","").trim();
        urlRepo.deleteByUrl(url);
    }
}
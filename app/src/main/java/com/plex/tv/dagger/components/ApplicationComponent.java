package com.plex.tv.dagger.components;


import com.plex.tv.App;
import com.plex.tv.dagger.AppScope;
import com.plex.tv.dagger.modules.ApplicationModule;
import com.plex.tv.dagger.modules.HttpClientModule;
import com.plex.tv.ui.main.MainFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by <a href="mailto:marcus@gabilheri.com">Marcus Gabilheri</a>
 *
 * @author Marcus Gabilheri
 * @version 1.0
 * @since 9/4/16.
 */
@AppScope
@Singleton
@Component(modules = {
        ApplicationModule.class,
        HttpClientModule.class,
})
public interface ApplicationComponent {

    void inject(App app);
    void inject(MainFragment mainFragment);
}

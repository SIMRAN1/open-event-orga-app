package org.fossasia.openevent.app.data.session;

import org.fossasia.openevent.app.common.Constants;
import org.fossasia.openevent.app.data.Repository;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SessionRepositoryImpl implements SessionRepository {

    private final SessionApi sessionApi;
    private final Repository repository;

    @Inject
    public SessionRepositoryImpl(SessionApi sessionApi, Repository repository) {
        this.sessionApi = sessionApi;
        this.repository = repository;
    }

    @Override
    public Observable<Session> getSessions(long trackId, boolean reload) {
        Observable<Session> diskObservable = Observable.defer(() ->
            repository.getItems(Session.class, Session_Table.track_id.eq(trackId))
        );

        Observable<Session> networkObservable = Observable.defer(() ->
            sessionApi.getSessions(trackId)
                .doOnNext(sessions -> repository.syncSave(Session.class, sessions, Session::getId, Session_Table.id).subscribe())
                .flatMapIterable(sessions -> sessions));

        return repository.observableOf(Session.class)
            .reload(reload)
            .withDiskObservable(diskObservable)
            .withNetworkObservable(networkObservable)
            .build();
    }

    @Override
    public Observable<Session> createSession(Session session) {
        if (!repository.isConnected()) {
            return Observable.error(new Throwable(Constants.NO_NETWORK));
        }

        return sessionApi
            .postSession(session)
            .doOnNext(created -> {
                created.setTrack(session.getTrack());
                created.setEvent(session.getEvent());
                repository
                    .save(Session.class, created)
                    .subscribe();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }
}

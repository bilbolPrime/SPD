package com.bilboldev.pixeldungeonskills.windows;

import java.util.ArrayList;
import java.util.List;

import com.bilboldev.noosa.NinePatch;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.audio.Sample;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.messages.Messages;
import com.bilboldev.pixeldungeonskills.network.PartyConnection;
import com.bilboldev.pixeldungeonskills.network.PartySession;
import com.bilboldev.pixeldungeonskills.network.PartyPrompts;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.HeroSprite;
import com.bilboldev.pixeldungeonskills.ui.RedButton;
import com.bilboldev.pixeldungeonskills.ui.RenderedTextMultiline;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import com.bilboldev.pixeldungeonskills.ui.Window;

public class WndPartyLobby extends WndScrollable {
    public interface StartListener {
        void start(PartySession.StartRequest request);
    }

    private final boolean hosting;
    private final Runnable cancel;
    private final StartListener start;
    private final RenderedTextMultiline heading;
    private final RedButton leave;
    private final RedButton action;
    private final ArrayList<PartyRow> rows = new ArrayList<>();
    private ScrollPane body;
    private String shownState;
    private PartyConnection.Peer shownRequest;
    private Window admission;
    private Window entry;
    private boolean failureShown;

    public WndPartyLobby(boolean hosting, Runnable cancel, StartListener start) {
        this.hosting = hosting;
        this.cancel = cancel;
        this.start = start;
        int width = boundedWidth(isDesktopRuntime() ? 300 : 180);
        heading = PixelScene.renderMultiline(text(hosting ? "host_title" : "join_title"), 10);
        heading.hardlight(titleColor());
        heading.maxWidth(width);
        leave = new ActionButton(text("cancel")) {
            @Override
            protected void onClick() {
                onBackPressed();
            }
        };
        action = new ActionButton(text(hosting ? "copy_code" : "enter_address")) {
            @Override
            protected void onClick() {
                if (hosting) {
                    PartyPrompts.copy(PartySession.pairingCode());
                } else {
                    enterAddress();
                }
            }
        };
        leave.setSize(width / 2f - 2, 20);
        action.setSize(width / 2f - 2, 20);
        refresh();
    }

    @Override
    public void update() {
        super.update();
        PartySession.StartRequest ready = PartySession.takeStart();
        if (ready != null) {
            hide();
            start.start(ready);
            return;
        }
        PartyConnection.Peer request = PartySession.request();
        if (admission != null && request != shownRequest) {
            admission.hide();
            admission = null;
        }
        if (request != null && request != shownRequest && Window.topWindow() == this) {
            shownRequest = request;
            admission = new WndOptions(HeroSprite.avatar(request.heroClass, 1), text("request_title"),
                    text("request_message",
                            Messages.capitalize(Messages.maybeTranslate(request.heroClass.title())),
                            request.address),
                    text("accept"), text("reject")) {
                @Override
                protected void onSelect(int index) {
                    admission = null;
                    if (PartySession.request() != request) return;
                    if (index == 0) {
                        PartySession.accept();
                    } else {
                        PartySession.reject();
                    }
                }

                @Override
                public void onBackPressed() {
                    if (PartySession.request() == request) PartySession.reject();
                    admission = null;
                    super.onBackPressed();
                }
            };
            parent.add(admission);
        }
        if (request == null) shownRequest = null;
        refresh();
        if (PartySession.lobbyState() != PartySession.LobbyState.ERROR) {
            failureShown = false;
        } else if (!failureShown && Window.topWindow() == this) {
            failureShown = true;
            parent.add(new WndOptions(text(hosting ? "host_failed" : "join_failed"),
                    PartySession.status() + "\n\n" + versionLabel(), text("back")));
        }
    }

    private void refresh() {
        List<PartyConnection.Host> hosts = PartySession.hosts();
        StringBuilder state = new StringBuilder(PartySession.lobbyState().name()).append(PartySession.status())
                .append(PartySession.pairingCode()).append(PartySession.hostAddresses()).append(PartySession.hostPort());
        for (PartyConnection.Host host : hosts) {
            state.append(host.id).append(host.name).append(host.address).append(':')
                    .append(host.port).append(host.heroClass);
        }
        if (state.toString().equals(shownState)) return;
        shownState = state.toString();
        if (body != null) {
            remove(body);
            body.destroy();
        }
        rows.clear();
        int width = boundedWidth(isDesktopRuntime() ? 300 : 180);
        String code = PartySession.pairingCode();
        heading.text(text(hosting ? "host_title" : "join_title")
                + (hosting && code != null && !code.isEmpty() ? "\n" + code : ""));
        Component content = new Component();
        RenderedTextMultiline version = PixelScene.renderMultiline(versionLabel(), 6);
        version.hardlight(0xA99CB8);
        version.maxWidth(textWidth(width));
        content.add(version);
        String message;
        switch (PartySession.lobbyState()) {
            case HOSTING:
                message = text("hosting");
                break;
            case DISCOVERING:
                message = text(!hosts.isEmpty() ? "choose_party" : "searching");
                break;
            case CONNECTING:
                message = text("connecting");
                break;
            case WAITING:
                message = text("waiting");
                break;
            case CONNECTED:
                message = text("connected");
                break;
            case ERROR:
                message = text(hosting ? "host_failed" : "join_failed");
                break;
            default:
                message = text("opening");
                break;
        }
        String status = PartySession.status();
        if (status != null && !status.isEmpty()) message += "\n\n" + status;
        if (hosting && PartySession.hostPort() != 0) {
            message += "\n";
            for (String address : PartySession.hostAddresses()) {
                message += "\n" + text("ip_address") + ": " + address;
            }
            message += "\n" + text("port") + ": " + PartySession.hostPort();
        }
        RenderedTextMultiline explanation = PixelScene.renderMultiline(message, 8);
        explanation.maxWidth(textWidth(width));
        explanation.setPos(0, version.bottom() + 6);
        content.add(explanation);
        float top = explanation.bottom() + 6;
        if (!hosting && PartySession.lobbyState() == PartySession.LobbyState.DISCOVERING) {
            for (PartyConnection.Host host : hosts) {
                PartyRow row = new PartyRow(host);
                row.setRect(0, top, textWidth(width), 26);
                content.add(row);
                rows.add(row);
                top = row.bottom() + 3;
            }
        }
        content.setSize(textWidth(width), top);
        boolean enabled = PartySession.lobbyState() == PartySession.LobbyState.DISCOVERING
                || PartySession.lobbyState() == PartySession.LobbyState.ERROR;
        if (hosting) enabled = code != null && !code.isEmpty();
        action.enable(enabled);
        layoutContent(width, heading, content, 4, action, leave);
    }

    @Override
    protected ScrollPane createScrollPane(Component content) {
        body = new ScrollPane(content) {
            @Override
            public void onClick(float x, float y) {
                for (PartyRow row : rows) {
                    if (row.inside(x, y)) {
                        Sample.INSTANCE.play(Assets.SND_CLICK);
                        enterCode(code -> {
                            failureShown = false;
                            PartySession.join(row.host, code);
                        });
                        break;
                    }
                }
            }
        };
        return body;
    }

    private void enterAddress() {
        entry = new WndPartyInput(true, (address, port, code) -> {
            if (exists && parent != null) {
                failureShown = false;
                PartySession.joinAddress(address, port, code);
            }
        });
        parent.add(entry);
    }

    private void enterCode(PartyPrompts.Result join) {
        entry = new WndPartyInput(false, (address, port, code) -> {
            if (exists && parent != null) join.complete(code);
        });
        parent.add(entry);
    }

    @Override
    public void destroy() {
        if (entry != null && entry.exists) entry.hide();
        super.destroy();
    }

    @Override
    public void onBackPressed() {
        PartySession.cancelLobby();
        cancel.run();
        super.onBackPressed();
    }

    private static String text(String key, Object... args) {
        return Messages.get(WndPartyLobby.class, key, args);
    }

    static String versionLabel() {
        return text("coop_version", PartyConnection.PROTOCOL);
    }

    private static class PartyRow extends Component {
        final PartyConnection.Host host;
        private final Image hero;
        private NinePatch frame;
        private RenderedTextMultiline name;
        private RenderedTextMultiline join;
        private NinePatch joinFrame;

        PartyRow(PartyConnection.Host host) {
            this.host = host;
            hero = HeroSprite.avatar(host.heroClass, 1);
            hero.scale.set(2);
            add(hero);
            String heroName = Messages.capitalize(Messages.maybeTranslate(host.heroClass.title()));
            name.text(heroName + "\n" + text("ip_address") + ": " + host.address
                    + "\n" + text("port") + ": " + host.port, false);
        }

        @Override
        protected void createChildren() {
            add(frame = Chrome.get(Chrome.Type.BUTTON));
            add(name = PixelScene.renderMultiline("", 8));
            add(joinFrame = Chrome.get(Chrome.Type.BUTTON));
            add(join = PixelScene.renderMultiline(text("join"), 8));
            join.hardlight(0xE7CBA5);
        }

        @Override
        protected void layout() {
            join.maxWidth((int)Math.min(72, width / 3) - 8);
            float joinWidth = Math.max(48, join.width() + 12);
            name.maxWidth(Math.max(1, (int)(width - joinWidth - hero.width() - 20)));
            height = Math.max(height, Math.max(hero.height(), Math.max(name.height(), join.height())) + 10);
            frame.x = x;
            frame.y = y;
            frame.size(width, height);
            hero.x = x + 4;
            hero.y = y + (height - hero.height()) / 2;
            name.setPos(hero.x + hero.width() + 4, y + (height - name.height()) / 2);
            joinFrame.x = right() - joinWidth - 3;
            joinFrame.y = y + 3;
            joinFrame.size(joinWidth, height - 6);
            join.setPos(joinFrame.x + (joinWidth - join.width()) / 2, y + (height - join.height()) / 2);
        }
    }
}

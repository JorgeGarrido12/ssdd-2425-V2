import time
from flask import Flask, render_template, send_from_directory, url_for, request, redirect
from flask_login import LoginManager, login_manager, current_user, login_user, login_required, logout_user
from models import User, users

#Importos para probar lo de usar sesion en vez de User.get_user(email) con REST.
from functools import wraps
from flask import redirect, url_for, session

import requests
import os

# Usuarios
from models import users, User

# Login
from forms import LoginForm, RegisterForm

app = Flask(__name__, static_url_path='')
login_manager = LoginManager()
login_manager.init_app(app) # Para mantener la sesión

# Configurar el secret_key. OJO, no debe ir en un servidor git público.
# Python ofrece varias formas de almacenar esto de forma segura, que
# no cubriremos aquí.
app.config['SECRET_KEY'] = 'qH1vprMjavek52cv7Lmfe1FoCexrrV8egFnB21jHhkuOHm8hJUe1hwn7pKEZQ1fioUzDb3sWcNK1pJVVIhyrgvFiIrceXpKJBFIn_i9-LTLBCc4cqaI3gjJJHU6kxuT8bnC7Ng'

#@app.route('/static/<path:path>')
#def serve_static(path):
#    return send_from_directory('static', path)

def session_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if "user_id" not in session or "token_privado" not in session:
            return redirect(url_for('login'))
        return f(*args, **kwargs)
    return decorated_function

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/login', methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('index'))

    error = None
    form = LoginForm(None if request.method != 'POST' else request.form)

    if request.method == 'POST' and form.validate():
        email = form.email.data
        password = form.password.data

        # Aquí llamamos al backend REST
        try:
            url_backend = "http://backend-rest:8080/Service/users/login"
            payload = {
                "email": email,
                "password": password
            }
            response = requests.post(url_backend, json=payload)

            if response.status_code == 200:
                data = response.json()
                # Ejemplo de respuesta esperada:
                # {
                #   "userId": "2",
                #   "token": "TOKENPRIVADO...."
                # }
                session["user_id"] = data["userId"]
                session["token_privado"] = data["token"]

                # También puedes hacer login_user() si mantienes el User local para Flask-Login
                #COMENTAMOS ESTO NO SABEMOS COMO LO VAMOS A DEJAR
                #user_found = User.get_user(email)
                #if user_found:
                #    login_user(user_found, remember=form.remember_me.data)

                #return redirect(url_for('profile'))

            else:
                error = f"Login incorrecto: {response.status_code} {response.text}"

        except Exception as e:
            error = f"Error de conexión con el backend: {str(e)}"

    return render_template('login.html', form=form, error=error)



@app.route('/signup', methods=['GET', 'POST'])
def signup():
    form = RegisterForm()

    if request.method == 'POST' and form.validate_on_submit():
        email = form.email.data
        name = form.username.data
        password = form.password.data
        password2 = form.confirm.data

        if password != password2:
            error = "Las contraseñas no coinciden"
            return render_template('signup.html', form=form, error=error)

        # Petición al backend REST
        try:
            url_backend = "http://backend-rest:8080/Service/signup"
            payload = {
                "email": email,
                "name": name,
                "password": password
            }

            response = requests.post(url_backend, json=payload)

            if response.status_code == 201:
                user_data = response.json()["user"]

                session["user_id"] = user_data["id"]
                session["token_privado"] = user_data["token"]

                return redirect(url_for('profile'))

            else:
                error = f"Error al registrar usuario: {response.status_code} {response.text}"
                return render_template('signup.html', form=form, error=error)

        except Exception as e:
            error = f"Error al conectar con el backend: {str(e)}"
            return render_template('signup.html', form=form, error=error)

    return render_template('signup.html', form=form)


@app.route('/profile')
#@login_required
@session_required
def profile():
    try:
        endpoint_url = f"/Service/users/id/{session['user_id']}"    # para Auth-Token
        full_url = f"http://backend-rest:8080{endpoint_url}"           # para requests.get()

        headers = build_auth_headers(endpoint_url)                  # para cabeceras

        response = requests.get(full_url, headers=headers)          # la llamada HTTP

        if response.status_code == 200:
            user_data = response.json()
            # Puedes pasar los datos al template:
            return render_template('profile.html', user_data=user_data)
        else:
            error = f"Error al obtener perfil: {response.status_code} {response.text}"
            return render_template('profile.html', error=error)

    except Exception as e:
        error = f"Error al conectar con el backend: {str(e)}"
        return render_template('profile.html', error=error)


@app.route('/logout')
#@login_required
@session_required
def logout():
    logout_user()
    return redirect(url_for('index'))

@app.route('/new-conversation', methods=['GET', 'POST'])
#@login_required
@session_required
def new_conversation():
    if request.method == 'POST':
        title = request.form.get('title')
        first_message = request.form.get('first_message')

        endpoint_url = f"/u/{session['user_id']}/dialogue"
        full_url = f"http://backend-rest:8080{endpoint_url}"
        headers = build_auth_headers(endpoint_url)

        payload = {
            "dialogue_id": title
            # puedes meter otros campos si tu endpoint REST los acepta
        }

        # Hacemos POST al backend REST
        response = requests.post(full_url, json=payload, headers=headers)

        if response.status_code == 201:
            # conversación creada → redirect a /conversations
            return redirect(url_for('conversations'))
        else:
            error = f"Error al crear conversación: {response.status_code} {response.text}"
            return render_template('new_conversation.html', error=error)

    return render_template('new_conversation.html')



@app.route('/conversations')
#@login_required
@session_required
def conversations():
    endpoint_url = f"/u/{session['user_id']}/dialogue"
    full_url = f"http://backend-rest:8080{endpoint_url}"
    headers = build_auth_headers(endpoint_url)

    response = requests.get(full_url, headers=headers)

    if response.status_code == 200:
        # El backend devuelve una lista de conversaciones
        convs = response.json()  # depende de cómo sea la respuesta concreta
        return render_template('conversations.html', convs=convs)
    else:
        error = f"Error al obtener conversaciones: {response.status_code} {response.text}"
        return render_template('conversations.html', error=error)




@app.route('/chat/<dialogue_id>', methods=['GET', 'POST'])
#@login_required
@session_required
def chat_detail(dialogue_id):
    # Primero obtenemos la conversación completa
    endpoint_url = f"/u/{session['user_id']}/dialogue/{dialogue_id}"
    full_url = f"http://backend-rest:8080{endpoint_url}"
    headers = build_auth_headers(endpoint_url)

    response = requests.get(full_url, headers=headers)

    if response.status_code != 200:
        error = f"Error al obtener conversación: {response.status_code} {response.text}"
        return render_template('chat_detail.html', error=error)

    conv = response.json()  # contiene dialogue_id, status, dialogue[], next, end

    # Si es POST → enviar un nuevo prompt
    if request.method == 'POST':
        prompt_text = request.form.get('prompt')
        if prompt_text and conv.get("status") == "READY":
            # hacemos POST al next URL
            next_url = conv["next"]  # OJO: es ya la URL relativa que da el backend
            full_next_url = f"http://backend-rest:8080{next_url}"

            headers_next = build_auth_headers(next_url)

            payload = {
                "prompt": prompt_text,
                "timestamp": int(time.time())
            }

            post_resp = requests.post(full_next_url, json=payload, headers=headers_next)

            if post_resp.status_code not in (200, 201, 202):
                error = f"Error al enviar prompt: {post_resp.status_code} {post_resp.text}"
                return render_template('chat_detail.html', conv=conv, error=error)

            # Redirigimos para recargar la conversación (para ver el nuevo mensaje)
            return redirect(url_for('chat_detail', dialogue_id=dialogue_id))

    # Si es GET → renderizamos la conversación
    return render_template('chat_detail.html', conv=conv)



@app.route('/end_conversation/<dialogue_id>', methods=['POST'])
#@login_required
@session_required
def end_conversation(dialogue_id):
    # Obtenemos la conversación primero para ver la URL "end"
    endpoint_url = f"/u/{session['user_id']}/dialogue/{dialogue_id}"
    full_url = f"http://backend-rest:8080{endpoint_url}"
    headers = build_auth_headers(endpoint_url)

    response = requests.get(full_url, headers=headers)

    if response.status_code != 200:
        error = f"Error al obtener conversación para terminarla: {response.status_code} {response.text}"
        return redirect(url_for('chat_detail', dialogue_id=dialogue_id))

    conv = response.json()

    end_url = conv.get("end")
    if not end_url:
        error = "No se puede terminar esta conversación."
        return redirect(url_for('chat_detail', dialogue_id=dialogue_id))

    # Hacemos POST al endpoint "end"
    full_end_url = f"http://backend-rest:8080{end_url}"
    headers_end = build_auth_headers(end_url)

    post_resp = requests.post(full_end_url, json={}, headers=headers_end)

    if post_resp.status_code in (200, 201, 202):
        # Ok, conversación terminada → volvemos al chat_detail
        return redirect(url_for('chat_detail', dialogue_id=dialogue_id))
    else:
        error = f"Error al terminar conversación: {post_resp.status_code} {post_resp.text}"
        return redirect(url_for('chat_detail', dialogue_id=dialogue_id))




@app.route('/delete_conversation/<dialogue_id>')
#@login_required
@session_required
def delete_conversation(dialogue_id):
    endpoint_url = f"/u/{session['user_id']}/dialogue/{dialogue_id}"
    full_url = f"http://backend-rest:8080{endpoint_url}"
    headers = build_auth_headers(endpoint_url)

    response = requests.delete(full_url, headers=headers)

    if response.status_code == 200:
        # Ok borrada
        return redirect(url_for('conversations'))
    else:
        error = f"Error al borrar conversación: {response.status_code} {response.text}"
        # Si quieres, podrías volver a conversations igual
        return redirect(url_for('conversations'))




@login_manager.user_loader
def load_user(user_id):
    for user in users:
        if user.id == int(user_id):
            return user
    return None

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 5010)))



def build_auth_headers(endpoint_url):
    from datetime import datetime
    import hashlib

    date_now = datetime.utcnow().isoformat() + "Z"
    data_to_hash = endpoint_url + date_now + session["token_privado"]
    auth_token = hashlib.md5(data_to_hash.encode("utf-8")).hexdigest()

    return {
        "User": session["user_id"],
        "Date": date_now,
        "Auth-Token": auth_token
    }
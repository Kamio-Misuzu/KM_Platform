import os
from datetime import datetime

from flask import Blueprint, request, jsonify, send_file
from flask_jwt_extended import jwt_required, get_jwt_identity
from werkzeug.utils import secure_filename
from models import User
from database import db
from config import Config

user_bp = Blueprint('user', __name__)


def allowed_file(filename):
    return '.' in filename and \
        filename.rsplit('.', 1)[1].lower() in Config.ALLOWED_EXTENSIONS


@user_bp.route('/<int:user_id>/avatar', methods=['POST'])
@jwt_required()
def upload_avatar(user_id):
    # 验证用户身份
    current_user_id = get_jwt_identity()
    if current_user_id != user_id:
        return jsonify({'error': 'Unauthorized'}), 403

    # 检查文件是否存在
    if 'avatar' not in request.files:
        return jsonify({'error': 'No file provided'}), 400

    file = request.files['avatar']
    if file.filename == '':
        return jsonify({'error': 'No file selected'}), 400

    if file and allowed_file(file.filename):
        # 安全处理文件名
        filename = secure_filename(file.filename)
        # 生成唯一文件名：用户ID + 时间戳 + 扩展名
        file_ext = filename.rsplit('.', 1)[1].lower()
        new_filename = f"{user_id}_{int(datetime.now().timestamp())}.{file_ext}"

        # 确保头像目录存在
        avatar_dir = os.path.join(Config.UPLOAD_FOLDER, 'avatars')
        if not os.path.exists(avatar_dir):
            os.makedirs(avatar_dir)

        # 保存文件
        file_path = os.path.join(avatar_dir, new_filename)
        file.save(file_path)

        # 更新用户头像信息
        user = User.query.get(user_id)
        if user:
            # 删除旧头像文件（如果存在）
            if user.avatar_url and os.path.exists(user.avatar_url):
                os.remove(user.avatar_url)

            # 更新数据库
            user.avatar_url = f"/api/users/{user_id}/avatar"
            user.avatar_type = file_ext
            db.session.commit()

            return jsonify({'message': 'Avatar uploaded successfully', 'avatar_url': user.avatar_url})

    return jsonify({'error': 'Invalid file type'}), 400


@user_bp.route('/<int:user_id>/avatar', methods=['GET'])
def get_avatar(user_id):
    user = User.query.get(user_id)
    if not user or not user.avatar_url:
        return jsonify({'error': 'Avatar not found'}), 404

    # 从文件名中提取实际存储路径
    avatar_dir = os.path.join(Config.UPLOAD_FOLDER, 'avatars')
    # 查找用户的最新头像文件
    avatar_files = [f for f in os.listdir(avatar_dir) if f.startswith(f"{user_id}_")]
    if not avatar_files:
        return jsonify({'error': 'Avatar file not found'}), 404

    # 获取最新的头像文件（按时间戳排序）
    latest_avatar = sorted(avatar_files, reverse=True)[0]
    file_path = os.path.join(avatar_dir, latest_avatar)

    if not os.path.exists(file_path):
        return jsonify({'error': 'Avatar file not found'}), 404

    return send_file(file_path)


@user_bp.route('/<int:user_id>', methods=['GET'])
@jwt_required()
def get_user(user_id):
    current_user_id = get_jwt_identity()
    if current_user_id != user_id:
        return jsonify({'error': 'Unauthorized'}), 403

    user = User.query.get(user_id)
    if not user:
        return jsonify({'error': 'User not found'}), 404

    return jsonify(user.to_dict())

@user_bp.route('/me', methods=['GET'])
@jwt_required()
def get_current_user():
    try:
        user_id = get_jwt_identity()
        user = User.query.get(user_id)

        if not user:
            return jsonify({'error': 'User not found'}), 404

        return jsonify(user.to_dict()), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500
